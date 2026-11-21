"""Riconoscimento di persone e pallone con YOLOX-Tiny (Apache-2.0) in formato ONNX.

Il modello è addestrato su COCO: usiamo le classi "person" (0) e "sports ball" (32).
Il file non è nel repository: si scarica con tools/download_models.py.
"""
from __future__ import annotations

import logging
import os
import threading
from dataclasses import dataclass
from pathlib import Path

import cv2
import numpy as np

log = logging.getLogger(__name__)

MODEL_PATH = Path(os.getenv("DETECTION_MODEL", Path(__file__).resolve().parent.parent / "models" / "yolox_tiny.onnx"))
PERSON, BALL = 0, 32
MIN_PERSON_CONFIDENCE = 0.35
MIN_BALL_CONFIDENCE = 0.15  # il pallone è piccolo e spesso sfocato: soglia più bassa, si filtra poi sul campo
NMS_IOU = 0.45


@dataclass(frozen=True)
class Detection:
    label: int
    confidence: float
    x1: float
    y1: float
    x2: float
    y2: float

    @property
    def height(self) -> float:
        return self.y2 - self.y1

    @property
    def foot(self) -> tuple[float, float]:
        """Punto di appoggio a terra: centro del lato inferiore del riquadro."""
        return (self.x1 + self.x2) / 2, self.y2

    @property
    def center(self) -> tuple[float, float]:
        return (self.x1 + self.x2) / 2, (self.y1 + self.y2) / 2


class Detector:
    """Wrapper thread-safe attorno alla sessione ONNX, caricata una sola volta."""

    _instance: Detector | None = None
    _lock = threading.Lock()

    def __init__(self, model_path: Path = MODEL_PATH) -> None:
        import onnxruntime as ort  # import pigro: il servizio parte anche senza

        options = ort.SessionOptions()
        options.intra_op_num_threads = max(1, (os.cpu_count() or 2) - 1)
        self._session = ort.InferenceSession(str(model_path), options, providers=["CPUExecutionProvider"])
        model_input = self._session.get_inputs()[0]
        self._input = model_input.name
        self.input_size = int(model_input.shape[-1])  # 416 per Tiny, 640 per S
        self._grids, self._strides = _grids(self.input_size)
        self._run_lock = threading.Lock()

    @classmethod
    def shared(cls) -> Detector | None:
        """Istanza condivisa, o None se modello o onnxruntime non sono disponibili."""
        with cls._lock:
            if cls._instance is None:
                if not MODEL_PATH.is_file():
                    log.warning("Modello di riconoscimento assente (%s): si usa il movimento dei pixel", MODEL_PATH)
                    return None
                try:
                    cls._instance = Detector()
                except Exception:  # noqa: BLE001 - qualsiasi problema di runtime ripiega sul metodo semplice
                    log.exception("Caricamento del modello di riconoscimento fallito")
                    return None
            return cls._instance

    def detect(self, frame_bgr: np.ndarray) -> list[Detection]:
        height, width = frame_bgr.shape[:2]
        size = self.input_size
        ratio = min(size / height, size / width)
        resized = cv2.resize(frame_bgr, (int(width * ratio), int(height * ratio)), interpolation=cv2.INTER_LINEAR)
        canvas = np.full((size, size, 3), 114, np.uint8)
        canvas[: resized.shape[0], : resized.shape[1]] = resized
        tensor = canvas.transpose(2, 0, 1)[None].astype(np.float32)  # YOLOX: BGR 0-255, senza normalizzazione

        with self._run_lock:
            raw = self._session.run(None, {self._input: tensor})[0][0]

        # decodifica delle griglie (l'export ufficiale restituisce valori grezzi)
        xy = (raw[:, :2] + self._grids) * self._strides
        wh = np.exp(raw[:, 2:4]) * self._strides
        boxes = np.concatenate([xy - wh / 2, xy + wh / 2], axis=1) / ratio

        detections: list[Detection] = []
        for label, threshold in ((PERSON, MIN_PERSON_CONFIDENCE), (BALL, MIN_BALL_CONFIDENCE)):
            scores = raw[:, 4] * raw[:, 5 + label]
            keep = np.where(scores >= threshold)[0]
            if not len(keep):
                continue
            rects = [[float(b[0]), float(b[1]), float(b[2] - b[0]), float(b[3] - b[1])] for b in boxes[keep]]
            chosen = cv2.dnn.NMSBoxes(rects, scores[keep].tolist(), threshold, NMS_IOU)
            for index in np.array(chosen).reshape(-1):
                x1, y1, x2, y2 = boxes[keep[index]]
                detections.append(Detection(
                    label, float(scores[keep[index]]),
                    float(np.clip(x1, 0, width)), float(np.clip(y1, 0, height)),
                    float(np.clip(x2, 0, width)), float(np.clip(y2, 0, height))))
        return detections

    def detect_tiled(self, frame_bgr: np.ndarray, region: tuple[int, int, int, int], tile: int) -> list[Detection]:
        """Rileva in riquadri sovrapposti dentro region (x0, y0, x1, y1): ingrandisce i soggetti piccoli.

        I riquadri si sovrappongono del 20% perché un giocatore sul bordo sia intero in almeno uno;
        i doppioni vengono poi uniti con una NMS sull'immagine intera.
        """
        x0, y0, x1, y1 = region
        height, width = frame_bgr.shape[:2]
        tile = max(32, min(tile, max(x1 - x0, y1 - y0)))
        step = max(16, int(tile * 0.8))
        found: list[Detection] = []
        for ty in _starts(y0, y1, tile, step):
            for tx in _starts(x0, x1, tile, step):
                crop = frame_bgr[ty:min(height, ty + tile), tx:min(width, tx + tile)]
                for d in self.detect(crop):
                    found.append(Detection(d.label, d.confidence, d.x1 + tx, d.y1 + ty, d.x2 + tx, d.y2 + ty))
        return _merge(found)


def _starts(lo: int, hi: int, tile: int, step: int) -> list[int]:
    if hi - lo <= tile:
        return [lo]
    starts = list(range(lo, hi - tile, step))
    starts.append(hi - tile)  # l'ultimo riquadro arriva al bordo
    return starts


def _merge(detections: list[Detection]) -> list[Detection]:
    merged: list[Detection] = []
    for label in {d.label for d in detections}:
        group = [d for d in detections if d.label == label]
        rects = [[d.x1, d.y1, d.x2 - d.x1, d.y2 - d.y1] for d in group]
        keep = cv2.dnn.NMSBoxes(rects, [d.confidence for d in group], 0.0, NMS_IOU)
        merged.extend(group[i] for i in np.array(keep).reshape(-1))
    return merged


def _grids(size: int) -> tuple[np.ndarray, np.ndarray]:
    grids, strides = [], []
    for stride in (8, 16, 32):
        cells = size // stride
        xv, yv = np.meshgrid(np.arange(cells), np.arange(cells))
        grids.append(np.stack((xv, yv), 2).reshape(-1, 2))
        strides.append(np.full((cells * cells, 1), stride))
    return np.concatenate(grids).astype(np.float32), np.concatenate(strides).astype(np.float32)
