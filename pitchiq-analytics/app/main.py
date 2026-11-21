"""
Servizio interno di elaborazione. Non è esposto al frontend: lo chiama solo il BFF.

POST /jobs  {"importId": 12, "cartella": "12"}  -> 202, elaborazione in background
POST /video-jobs  {"jobId": "<uuid>"}            -> 202, estrazione clip candidate
GET  /video-jobs/{id}                            -> stato e segmenti rilevati (con proposte del modello eventi)
POST /event-model  {"esempi": [...]}             -> riaddestra il classificatore eventi dalle etichette
GET  /event-model                                -> stato del classificatore eventi
GET  /health
"""
from __future__ import annotations

import json
import logging
import os
import shutil
import threading
import time
from typing import Literal
from uuid import UUID
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from . import db
from .pipeline import InvalidInput, process
from .event_model import LABELS, EventModel
from .video_pipeline import VideoInputError, analyze_video

log = logging.getLogger("pitchiq.analytics")
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")

UPLOAD_ROOT = Path(os.getenv("TRACKING_UPLOAD_DIR", "/data/tracking")).resolve()
KEEP_FILES = os.getenv("TRACKING_KEEP_FILES", "false").lower() == "true"

app = FastAPI(title="PitchIQ Analytics", version="1.0.0")
# un job alla volta: il tracking pesa ~90 MB e il calcolo è CPU-bound
executor = ThreadPoolExecutor(max_workers=int(os.getenv("ANALYTICS_WORKERS", "1")))
_engine = None


def engine():
    global _engine
    if _engine is None:
        _engine = db.engine_from_env()
    return _engine


class JobRequest(BaseModel):
    importId: int = Field(gt=0)
    cartella: str = Field(min_length=1, max_length=100)


class VideoJobRequest(BaseModel):
    jobId: UUID


class EventExample(BaseModel):
    etichetta: Literal[LABELS]  # type: ignore[valid-type]
    caratteristiche: dict[str, float]


class EventTrainingRequest(BaseModel):
    esempi: list[EventExample] = Field(max_length=10_000)


EVENT_MODEL_PATH = UPLOAD_ROOT / "event-model.json"
_event_model_lock = threading.Lock()
_event_model: EventModel | None = None


def event_model() -> EventModel:
    global _event_model
    with _event_model_lock:
        if _event_model is None:
            _event_model = EventModel.load(EVENT_MODEL_PATH)
        return _event_model


def video_job_dir(job_id: UUID) -> Path:
    return UPLOAD_ROOT / "video-jobs" / str(job_id)


def save_video_state(job_dir: Path, state: dict) -> None:
    path = job_dir / "state.json"
    temporary = path.with_suffix(".tmp")
    temporary.write_text(json.dumps(state, ensure_ascii=False), encoding="utf-8")
    temporary.replace(path)


def run_video_job(job_id: UUID, job_dir: Path) -> None:
    state = {"id": str(job_id), "stato": "IN_ELABORAZIONE", "messaggio": None,
             "durataSecondi": None, "eventi": []}
    save_video_state(job_dir, state)
    try:
        result = analyze_video(job_dir / "source", job_dir / "clips")
        state.update(stato="COMPLETATO", durataSecondi=result["durataSecondi"],
                     eventi=result["eventi"], movimento=result["movimento"],
                     messaggio=f"{len(result['eventi'])} clip candidate da verificare.")
        save_video_state(job_dir, state)
    except VideoInputError as exc:
        shutil.rmtree(job_dir / "clips", ignore_errors=True)
        state.update(stato="ERRORE", messaggio=str(exc))
        save_video_state(job_dir, state)
        log.warning("analisi video %s rifiutata: %s", job_id, exc)
    except Exception:
        shutil.rmtree(job_dir / "clips", ignore_errors=True)
        log.exception("analisi video %s fallita", job_id)
        state.update(stato="ERRORE", messaggio="Errore interno durante l'analisi video.")
        save_video_state(job_dir, state)
    finally:
        (job_dir / "source").unlink(missing_ok=True)


def run_job(import_id: int, job_dir: Path) -> None:
    t0 = time.monotonic()
    completed = False
    try:
        db.set_status(engine(), import_id, "IN_ELABORAZIONE")
        result = process(job_dir)
        db.save_result(engine(), import_id, result)
        n_sup = sum(s["esito"] == "SUPERATA" for s in result.sequenze)
        msg = (f"{len(result.sequenze)} possessi pressati, {n_sup} pressioni superate, "
               f"{len(result.animazioni)} sequenze animate.")
        db.set_status(engine(), import_id, "COMPLETATO", msg, result.partita["match_id"])
        completed = True
        log.info("import %s completato in %.1fs: %s", import_id, time.monotonic() - t0, msg)
    except InvalidInput as e:
        db.set_status(engine(), import_id, "ERRORE", str(e))
        log.warning("import %s rifiutato: %s", import_id, e)
    except db.DuplicateMatch as e:
        db.set_status(engine(), import_id, "ERRORE", str(e))
        log.warning("import %s duplicato: %s", import_id, e)
    except Exception:  # noqa: BLE001 - qualunque errore deve chiudere l'import, non lasciarlo appeso
        log.exception("import %s fallito", import_id)
        db.set_status(engine(), import_id, "ERRORE",
                      "Errore interno durante l'elaborazione. Controlla i log di pitchiq-analytics.")
    finally:
        if completed and not KEEP_FILES:
            shutil.rmtree(job_dir, ignore_errors=True)


@app.post("/jobs", status_code=202)
def submit(req: JobRequest):
    job_dir = (UPLOAD_ROOT / req.cartella).resolve()
    if UPLOAD_ROOT not in job_dir.parents or not job_dir.is_dir():
        raise HTTPException(400, f"Cartella non trovata: {req.cartella}")
    executor.submit(run_job, req.importId, job_dir)
    return {"importId": req.importId, "stato": "IN_CODA"}


@app.post("/video-jobs", status_code=202)
def submit_video(req: VideoJobRequest):
    job_dir = video_job_dir(req.jobId).resolve()
    if UPLOAD_ROOT not in job_dir.parents or not (job_dir / "source").is_file():
        raise HTTPException(400, "Video non trovato.")
    if (job_dir / "state.json").exists():
        raise HTTPException(409, "Analisi video già avviata.")
    save_video_state(job_dir, {"id": str(req.jobId), "stato": "IN_CODA",
                               "messaggio": None, "durataSecondi": None, "eventi": []})
    executor.submit(run_video_job, req.jobId, job_dir)
    return {"id": str(req.jobId), "stato": "IN_CODA"}


@app.get("/video-jobs/{job_id}")
def video_status(job_id: UUID):
    state_file = video_job_dir(job_id) / "state.json"
    if not state_file.is_file():
        raise HTTPException(404, "Analisi video non trovata.")
    state = json.loads(state_file.read_text(encoding="utf-8"))
    # proposte calcolate a ogni lettura: riflettono sempre il modello più recente, anche per analisi vecchie
    model = event_model()
    for event in state.get("eventi", []):
        features = event.get("caratteristiche")
        proposal = model.propose(features) if isinstance(features, dict) else None
        event["proposta"] = (
            {"etichetta": proposal.label, "confidenza": round(proposal.confidence, 3)} if proposal else None
        )
    return state


@app.post("/event-model")
def train_event_model(req: EventTrainingRequest):
    global _event_model
    examples = [e.model_dump() for e in req.esempi]
    model = EventModel.train(examples)
    UPLOAD_ROOT.mkdir(parents=True, exist_ok=True)
    with _event_model_lock:
        model.save(EVENT_MODEL_PATH, examples)
        _event_model = model
    summary = model.summary()
    log.info("modello eventi riaddestrato: %s esempi, attivo=%s", summary["esempi"], summary["attivo"])
    return summary


@app.get("/event-model")
def event_model_status():
    return event_model().summary()


@app.get("/health")
def health():
    return {"status": "UP"}
