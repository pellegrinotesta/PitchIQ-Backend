"""Estrarre clip candidate dai momenti di maggiore attività in campo.

Metodo principale: riconoscimento di giocatori e pallone (YOLOX) e loro velocità al netto della camera.
Ripiego: differenza tra fotogrammi sul campo, se il modello non c'è o il video non inquadra una partita.
"""
from __future__ import annotations

from collections import deque
from dataclasses import dataclass, field
from pathlib import Path
from statistics import median

import cv2
import numpy as np

from .camera_motion import frame_difference, pitch_mask
from .detection import Detector
from .player_activity import FieldState, activity, field_state, scale_transform
from .video_segments import MotionSample, candidate_intervals, motion_threshold

MAX_DURATION_SECONDS = 120
SAMPLE_INTERVAL_SECONDS = 0.5
SMALL_SIZE = (320, 180)
# sotto questa quota mediana di campo inquadrato il video non è una ripresa di campo (es. palestra): niente maschera
MIN_PITCH_VIDEO = 0.15
# inquadrature in cui il campo quasi non si vede (pubblico, panchina): non contano come azione
MIN_PITCH_FRAME = 0.05
# con meno giocatori riconosciuti in mediana il video non è una ripresa di gioco: si torna ai pixel
MIN_PLAYERS_VIDEO = 4
# altezza in pixel che un giocatore deve avere nell'ingresso del modello per essere riconosciuto bene
TARGET_PLAYER_HEIGHT = 40
# stima iniziale dell'altezza di un giocatore rispetto al fotogramma (inquadratura larga da tribuna)
INITIAL_PLAYER_RATIO = 0.04

METHOD_PLAYERS = "giocatori"
METHOD_PIXELS = "pixel"


class VideoInputError(ValueError):
    """Video illeggibile o fuori dai limiti supportati."""


@dataclass
class MotionAnalysis:
    samples: list[MotionSample]
    duration: float
    frames: int
    method: str
    players: list[int] = field(default_factory=list)
    ball: list[bool] = field(default_factory=list)


class _PlayerTracker:
    """Rileva i giocatori campione per campione, adattando l'ingrandimento alla loro altezza."""

    def __init__(self, detector: Detector, frame_height: int) -> None:
        self.detector = detector
        self.recent_heights: deque[float] = deque(maxlen=60)
        self.initial_height = INITIAL_PLAYER_RATIO * frame_height
        self.previous: FieldState | None = None

    def player_height(self) -> float:
        return median(self.recent_heights) if self.recent_heights else self.initial_height

    def observe(self, second: float, frame: np.ndarray, pitch_full: np.ndarray) -> FieldState | None:
        ys, xs = np.nonzero(pitch_full)
        if not len(xs):
            return None
        height, width = frame.shape[:2]
        player = self.player_height()
        # sopra il bordo del campo spuntano le teste dei giocatori sul lato lontano
        region = (int(xs.min()), max(0, int(ys.min() - 1.5 * player)), int(xs.max()) + 1, int(ys.max()) + 1)
        tile = int(self.detector.input_size * player / TARGET_PLAYER_HEIGHT)
        detections = self.detector.detect_tiled(frame, region, max(160, tile))
        state = field_state(second, detections, pitch_full, height)
        self.recent_heights.extend(p.height for p in state.players)
        return state


def _motion_samples(video_path: Path) -> MotionAnalysis:
    """Campiona ogni SAMPLE_INTERVAL_SECONDS usando i timestamp reali dei fotogrammi.

    Il frame rate dichiarato dal container non è affidabile (es. WebM a frame rate variabile,
    dove OpenCV riporta 120 fps per un video a 30): restituisce anche durata e numero di
    fotogrammi misurati durante la lettura.
    """
    capture = cv2.VideoCapture(str(video_path))
    if not capture.isOpened():
        raise VideoInputError("Il file non è un video leggibile o il codec non è supportato.")
    detector = Detector.shared()
    tracker: _PlayerTracker | None = None
    full: list[MotionSample] = []
    on_pitch: list[MotionSample] = []
    by_players: list[MotionSample] = []
    fractions: list[float] = []
    players_count: list[int] = []
    ball_seen: list[bool] = []
    previous = None
    previous_pitch = None
    previous_state: FieldState | None = None
    next_sample = 0.0
    frames = 0
    last_second = 0.0
    try:
        while True:
            ok, frame = capture.read()
            if not ok:
                break
            frames += 1
            second = capture.get(cv2.CAP_PROP_POS_MSEC) / 1000
            last_second = max(last_second, second)
            if second + 1e-6 < next_sample:
                continue
            next_sample = second + SAMPLE_INTERVAL_SECONDS
            small = cv2.resize(frame, SMALL_SIZE, interpolation=cv2.INTER_AREA)
            gray = cv2.cvtColor(small, cv2.COLOR_BGR2GRAY)
            pitch = pitch_mask(small)
            height, width = frame.shape[:2]

            state = None
            pitch_fraction = cv2.countNonZero(pitch) / pitch.size
            if detector is not None and pitch_fraction >= MIN_PITCH_FRAME:
                tracker = tracker or _PlayerTracker(detector, height)
                pitch_full = cv2.resize(pitch, (width, height), interpolation=cv2.INTER_NEAREST)
                state = tracker.observe(second, frame, pitch_full)

            if previous is not None:
                # differenza al netto di panoramica/zoom della camera, misurata anche solo sul campo
                diff = frame_difference(previous, gray, pitch, previous_pitch)
                full.append(MotionSample(second, diff.score))
                pitch_score = diff.pitch_score if diff.pitch_fraction >= MIN_PITCH_FRAME else 0.0
                on_pitch.append(MotionSample(second, pitch_score))
                fractions.append(diff.pitch_fraction)

                score = 0.0
                if state is not None and previous_state is not None:
                    camera = scale_transform(diff.transform, SMALL_SIZE, (width, height))
                    score = activity(previous_state, state, camera).score
                by_players.append(MotionSample(second, score))
                players_count.append(len(state.players) if state else 0)
                ball_seen.append(bool(state and state.ball))
            previous = gray
            previous_pitch = pitch
            previous_state = state
    finally:
        capture.release()

    if detector is not None and players_count and median(players_count) >= MIN_PLAYERS_VIDEO:
        return MotionAnalysis(by_players, last_second, frames, METHOD_PLAYERS, players_count, ball_seen)
    # il movimento di pubblico, bandiere e fumo non è gioco: se il campo è inquadrato si guarda solo lì
    use_pitch = bool(fractions) and median(fractions) >= MIN_PITCH_VIDEO
    return MotionAnalysis(on_pitch if use_pitch else full, last_second, frames, METHOD_PIXELS)


def _write_clip(video_path: Path, target: Path, start: float, end: float,
                fps: float, width: int, height: int) -> None:
    capture = cv2.VideoCapture(str(video_path))
    if not capture.isOpened():
        raise VideoInputError("Non è stato possibile riaprire il video per creare le clip.")
    writer = cv2.VideoWriter(str(target), cv2.VideoWriter_fourcc(*"mp4v"), fps, (width, height))
    if not writer.isOpened():
        capture.release()
        raise VideoInputError("Il server non riesce a codificare le clip video in MP4.")

    try:
        # lettura sequenziale: il seek per tempo è impreciso su alcuni container (WebM)
        while True:
            ok, frame = capture.read()
            if not ok:
                break
            second = capture.get(cv2.CAP_PROP_POS_MSEC) / 1000
            if second < start:
                continue
            if second > end:
                break
            writer.write(frame)
    finally:
        writer.release()
        capture.release()

    if not target.is_file() or target.stat().st_size == 0:
        raise VideoInputError("Non è stato possibile generare una clip dal video.")


def analyze_video(video_path: Path, clips_dir: Path) -> dict:
    capture = cv2.VideoCapture(str(video_path))
    if not capture.isOpened():
        raise VideoInputError("Il file non è un video leggibile o il codec non è supportato.")
    fps = float(capture.get(cv2.CAP_PROP_FPS))
    frame_count = int(capture.get(cv2.CAP_PROP_FRAME_COUNT))
    width = int(capture.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(capture.get(cv2.CAP_PROP_FRAME_HEIGHT))
    capture.release()

    if fps <= 0 or frame_count <= 0 or width <= 0 or height <= 0:
        raise VideoInputError("Il video non contiene fotogrammi leggibili.")
    duration = frame_count / fps
    if duration > MAX_DURATION_SECONDS:
        raise VideoInputError("La durata massima supportata è di 2 minuti.")
    if duration < 2:
        raise VideoInputError("Il video deve durare almeno 2 secondi.")

    analysis = _motion_samples(video_path)
    samples = analysis.samples
    if analysis.duration > 0 and analysis.frames > 1:
        duration = analysis.duration
        fps = analysis.frames / analysis.duration
    intervals = candidate_intervals(samples, duration)
    clips_dir.mkdir(parents=True, exist_ok=True)
    peak = max((sample.score for sample in samples), default=0.0)
    events = []
    for index, (start, end, intensity) in enumerate(intervals, start=1):
        clip_id = f"clip-{index:03d}"
        _write_clip(video_path, clips_dir / f"{clip_id}.mp4", start, end, fps, width, height)
        events.append({
            "id": clip_id,
            "etichetta": _label(analysis, start, end),
            "inizioSecondi": round(start, 2),
            "fineSecondi": round(end, 2),
            "intensitaMovimento": round(intensity / peak, 3) if peak else 0.0,
        })

    threshold = motion_threshold(samples)
    with_players = analysis.method == METHOD_PLAYERS
    movement = {
        "metodo": analysis.method,
        "soglia": round(threshold, 2) if threshold is not None else None,
        "campioni": [
            {"secondo": round(s.second, 2), "valore": round(s.score, 2),
             **({"giocatori": analysis.players[i], "palla": analysis.ball[i]} if with_players else {})}
            for i, s in enumerate(samples)
        ],
    }
    return {"durataSecondi": round(duration, 2), "eventi": events, "movimento": movement}


def _label(analysis: MotionAnalysis, start: float, end: float) -> str:
    if analysis.method != METHOD_PLAYERS:
        return "Azione da verificare"
    ball = any(seen for s, seen in zip(analysis.samples, analysis.ball) if start <= s.second <= end)
    return "Azione con pallone in vista" if ball else "Fase di gioco intensa"
