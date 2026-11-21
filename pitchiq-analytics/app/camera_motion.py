"""Differenza tra due fotogrammi al netto del movimento della camera.

Con una camera che segue il gioco quasi tutta l'immagine cambia tra un campione e l'altro anche
quando in campo succede poco. Stimiamo il movimento globale (panoramica, zoom, leggera rotazione)
dai punti dello sfondo, riallineiamo il fotogramma precedente sul corrente e misuriamo la
differenza solo dove i due fotogrammi si sovrappongono: resta il movimento dei giocatori.
"""
from __future__ import annotations

from dataclasses import dataclass, field, replace

import cv2
import numpy as np

MIN_INLIERS = 12
BLUR_KERNEL = (5, 5)
# area minima sovrapposta perché la misura sia significativa (es. dopo una panoramica molto rapida)
MIN_OVERLAP = 0.25


@dataclass(frozen=True)
class FrameDifference:
    score: float
    """Differenza media di luminosità (0-255) nella zona comune, dopo il riallineamento."""
    camera_shift: float
    """Spostamento stimato della camera, in pixel del fotogramma ridotto."""
    compensated: bool
    """False se non è stato possibile stimare il movimento: score è la differenza grezza."""
    pitch_score: float = 0.0
    """Come score, ma solo dentro il campo da gioco (0 se il campo non è inquadrato)."""
    pitch_fraction: float = 0.0
    """Quota del fotogramma occupata dal campo."""
    transform: np.ndarray | None = field(default=None, compare=False)
    """Affine 2x3 stimata da previous a current (coordinate del fotogramma ridotto), se disponibile."""


def pitch_mask(frame_bgr: np.ndarray) -> np.ndarray:
    """Maschera del terreno di gioco: pixel verdi, con i buchi lasciati da giocatori e linee richiusi."""
    hsv = cv2.cvtColor(frame_bgr, cv2.COLOR_BGR2HSV)
    # tonalità da 36: sotto c'è il giallo fluo dei gilet di steward e fotografi
    grass = cv2.inRange(hsv, (36, 40, 35), (90, 255, 255))
    # i giocatori interrompono l'erba: la chiusura li riassorbe nel campo, l'apertura toglie il verde sparso (cartelloni, maglie)
    grass = cv2.morphologyEx(grass, cv2.MORPH_OPEN, np.ones((3, 3), np.uint8))
    grass = cv2.morphologyEx(grass, cv2.MORPH_CLOSE, cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (15, 15)))
    # il campo è un'unica zona continua: chiazze verdi staccate (cartelloni, gilet, maglie) non sono campo
    count, labels, stats, _ = cv2.connectedComponentsWithStats(grass, connectivity=8)
    if count <= 1:
        return grass
    largest = 1 + int(np.argmax(stats[1:, cv2.CC_STAT_AREA]))
    return np.where(labels == largest, 255, 0).astype(np.uint8)


def _estimate_transform(previous: np.ndarray, current: np.ndarray) -> np.ndarray | None:
    """Trasformazione affine parziale (traslazione, rotazione, scala) da previous a current."""
    # 1) stima grossolana della panoramica: robusta anche con spostamenti ampi e motivi ripetuti
    #    (linee del campo, file di seggiolini) su cui il flusso ottico da solo si aggancia al periodo sbagliato
    (dx, dy), response = cv2.phaseCorrelate(np.float32(previous), np.float32(current))
    translation = np.float32([[1, 0, dx], [0, 1, dy]]) if response >= 0.1 else None

    # 2) rifinitura con i punti dello sfondo, partendo dalla stima precedente
    points = cv2.goodFeaturesToTrack(previous, maxCorners=300, qualityLevel=0.01, minDistance=6, blockSize=5)
    if points is not None and len(points) >= MIN_INLIERS:
        guess = points + np.float32([dx, dy]) if translation is not None else points.copy()
        moved, status, _ = cv2.calcOpticalFlowPyrLK(
            previous, current, points, guess, winSize=(21, 21), maxLevel=3,
            criteria=(cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT, 30, 0.01),
            flags=cv2.OPTFLOW_USE_INITIAL_FLOW)
        ok = status.reshape(-1) == 1
        if ok.sum() >= MIN_INLIERS:
            # RANSAC: i giocatori in movimento sono minoranza rispetto ai punti dello sfondo e vengono scartati
            matrix, inliers = cv2.estimateAffinePartial2D(
                points[ok], moved[ok], method=cv2.RANSAC, ransacReprojThreshold=2.0,
                maxIters=2000, confidence=0.99)
            if matrix is not None and inliers is not None and int(inliers.sum()) >= MIN_INLIERS:
                return matrix

    # ripiego per inquadrature povere di dettagli (es. solo erba): sola traslazione
    return translation


def frame_difference(previous: np.ndarray, current: np.ndarray,
                     pitch: np.ndarray | None = None,
                     previous_pitch: np.ndarray | None = None) -> FrameDifference:
    """previous e current: fotogrammi in scala di grigi della stessa dimensione.
    pitch / previous_pitch: maschere opzionali del campo nei due fotogrammi (vedi pitch_mask).
    Conta solo ciò che è campo in entrambi: mani e teste davanti all'obiettivo coprono il campo
    in uno dei due fotogrammi e restano fuori dalla misura."""
    a = cv2.GaussianBlur(previous, BLUR_KERNEL, 0)
    b = cv2.GaussianBlur(current, BLUR_KERNEL, 0)
    raw = float(cv2.absdiff(a, b).mean())

    matrix = _estimate_transform(previous, current)
    if matrix is None:
        both = _intersect(pitch, previous_pitch)
        return _with_pitch(FrameDifference(raw, 0.0, False), cv2.absdiff(a, b), None, both, pitch)

    height, width = b.shape
    aligned = cv2.warpAffine(a, matrix, (width, height), flags=cv2.INTER_LINEAR, borderValue=0)
    valid = cv2.warpAffine(np.full_like(a, 255), matrix, (width, height), flags=cv2.INTER_NEAREST, borderValue=0)
    # bordo di sicurezza: l'interpolazione ai margini della zona valida produce falsi contorni
    valid = cv2.erode(valid, np.ones((5, 5), np.uint8))
    overlap = cv2.countNonZero(valid)
    if overlap < MIN_OVERLAP * width * height:
        both = _intersect(pitch, previous_pitch)
        return _with_pitch(FrameDifference(raw, _shift(matrix), False, transform=matrix),
                           cv2.absdiff(a, b), None, both, pitch)

    diff = cv2.absdiff(aligned, b)
    score = float(cv2.mean(diff, mask=valid)[0])
    warped_previous_pitch = None if previous_pitch is None else cv2.warpAffine(
        previous_pitch, matrix, (width, height), flags=cv2.INTER_NEAREST, borderValue=0)
    both = _intersect(pitch, warped_previous_pitch)
    return _with_pitch(FrameDifference(score, _shift(matrix), True, transform=matrix), diff, valid, both, pitch)


def _intersect(pitch: np.ndarray | None, other: np.ndarray | None) -> np.ndarray | None:
    if pitch is None or other is None:
        return pitch
    return cv2.bitwise_and(pitch, other)


def _with_pitch(result: FrameDifference, diff: np.ndarray, valid: np.ndarray | None,
                region_pitch: np.ndarray | None, pitch: np.ndarray | None) -> FrameDifference:
    if region_pitch is None or pitch is None:
        return result
    region = region_pitch if valid is None else cv2.bitwise_and(region_pitch, valid)
    pixels = cv2.countNonZero(region)
    fraction = cv2.countNonZero(pitch) / pitch.size
    pitch_score = float(cv2.mean(diff, mask=region)[0]) if pixels else 0.0
    return replace(result, pitch_score=pitch_score, pitch_fraction=fraction)


def _shift(matrix: np.ndarray) -> float:
    return float(np.hypot(matrix[0, 2], matrix[1, 2]))
