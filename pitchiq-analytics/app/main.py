"""
Servizio interno di elaborazione. Non è esposto al frontend: lo chiama solo il BFF.

POST /jobs  {"importId": 12, "cartella": "12"}  -> 202, elaborazione in background
GET  /health
"""
from __future__ import annotations

import logging
import os
import shutil
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from . import db
from .pipeline import InvalidInput, process

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


def run_job(import_id: int, job_dir: Path) -> None:
    t0 = time.monotonic()
    try:
        db.set_status(engine(), import_id, "IN_ELABORAZIONE")
        result = process(job_dir)
        db.save_result(engine(), import_id, result)
        n_sup = sum(s["esito"] == "SUPERATA" for s in result.sequenze)
        msg = (f"{len(result.sequenze)} possessi pressati, {n_sup} pressioni superate, "
               f"{len(result.animazioni)} sequenze animate.")
        db.set_status(engine(), import_id, "COMPLETATO", msg, result.partita["match_id"])
        log.info("import %s completato in %.1fs: %s", import_id, time.monotonic() - t0, msg)
    except InvalidInput as e:
        db.set_status(engine(), import_id, "ERRORE", str(e))
        log.warning("import %s rifiutato: %s", import_id, e)
    except Exception:  # noqa: BLE001 - qualunque errore deve chiudere l'import, non lasciarlo appeso
        log.exception("import %s fallito", import_id)
        db.set_status(engine(), import_id, "ERRORE",
                      "Errore interno durante l'elaborazione. Controlla i log di pitchiq-analytics.")
    finally:
        if not KEEP_FILES:
            shutil.rmtree(job_dir, ignore_errors=True)


@app.post("/jobs", status_code=202)
def submit(req: JobRequest):
    job_dir = (UPLOAD_ROOT / req.cartella).resolve()
    if UPLOAD_ROOT not in job_dir.parents or not job_dir.is_dir():
        raise HTTPException(400, f"Cartella non trovata: {req.cartella}")
    executor.submit(run_job, req.importId, job_dir)
    return {"importId": req.importId, "stato": "IN_CODA"}


@app.get("/health")
def health():
    return {"status": "UP"}
