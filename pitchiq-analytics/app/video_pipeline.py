"""Estrarre clip candidate da intervalli di movimento visivo intenso."""
from __future__ import annotations

from pathlib import Path

import cv2

from .video_segments import MotionSample, candidate_intervals

MAX_DURATION_SECONDS = 120
SAMPLE_INTERVAL_SECONDS = 0.5


class VideoInputError(ValueError):
    """Video illeggibile o fuori dai limiti supportati."""


def _motion_samples(video_path: Path, fps: float, frame_count: int) -> list[MotionSample]:
    capture = cv2.VideoCapture(str(video_path))
    if not capture.isOpened():
        raise VideoInputError("Il file non è un video leggibile o il codec non è supportato.")
    samples: list[MotionSample] = []
    previous = None
    stride = max(1, round(fps * SAMPLE_INTERVAL_SECONDS))
    frame_index = 0
    try:
        while frame_index < frame_count:
            ok, frame = capture.read()
            if not ok:
                break
            if frame_index % stride == 0:
                small = cv2.resize(frame, (320, 180), interpolation=cv2.INTER_AREA)
                gray = cv2.cvtColor(small, cv2.COLOR_BGR2GRAY)
                if previous is not None:
                    score = float(cv2.absdiff(previous, gray).mean())
                    samples.append(MotionSample(frame_index / fps, score))
                previous = gray
            frame_index += 1
    finally:
        capture.release()
    return samples


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
        capture.set(cv2.CAP_PROP_POS_MSEC, start * 1000)
        frame_limit = round((end - start) * fps)
        for _ in range(frame_limit):
            ok, frame = capture.read()
            if not ok:
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

    samples = _motion_samples(video_path, fps, frame_count)
    intervals = candidate_intervals(samples, duration)
    clips_dir.mkdir(parents=True, exist_ok=True)
    events = []
    for index, (start, end, intensity) in enumerate(intervals, start=1):
        clip_id = f"clip-{index:03d}"
        _write_clip(video_path, clips_dir / f"{clip_id}.mp4", start, end, fps, width, height)
        peak = max((sample.score for sample in samples), default=0.0)
        events.append({
            "id": clip_id,
            "etichetta": "Azione da verificare",
            "inizioSecondi": round(start, 2),
            "fineSecondi": round(end, 2),
            "intensitaMovimento": round(intensity / peak, 3) if peak else 0.0,
        })

    return {"durataSecondi": round(duration, 2), "eventi": events}
