"""Attività dei giocatori tra due campioni: quanto si muovono in campo, al netto della camera.

Le velocità sono in "altezze del corpo al secondo": un giocatore alto 20 px da tribuna e uno alto
200 px in primo piano che scattano allo stesso modo danno lo stesso valore, qualunque sia lo zoom.
Riferimento: camminata ~0,5-1, corsa ~2-3, scatto ~4-5 altezze/s.
"""
from __future__ import annotations

from dataclasses import dataclass

import numpy as np

from .detection import BALL, PERSON, Detection

TOP_PLAYERS = 3
"""L'attività è la media dei giocatori più veloci: uno scatto o un contrasto contano anche se gli altri sono fermi."""
MAX_MATCH_HEIGHTS = 3.0
"""Oltre questa distanza (in altezze del giocatore) due rilevamenti non sono lo stesso giocatore."""
MAX_PLAYER_SPEED = 8.0
"""Velocità oltre questo limite sono abbinamenti sbagliati, non giocatori."""
MAX_BALL_SPEED = 30.0
BALL_WEIGHT = 0.3
"""Il pallone va ~10 volte più veloce dei giocatori: pesato perché un tiro conti quanto uno scatto, non dieci."""
MIN_BALL_CONFIDENCE = 0.3
BALL_SIZE_RANGE = (0.04, 0.35)
"""Diametro plausibile del pallone rispetto all'altezza dei giocatori (reale ~0,12): scarta teste, luci, cartelloni."""
MAX_PLAYER_HEIGHT_RATIO = 0.35
"""Persone più alte di così rispetto al fotogramma sono spettatori in primo piano."""


@dataclass(frozen=True)
class FieldState:
    """Giocatori e pallone in un campione, in coordinate del fotogramma originale."""
    second: float
    players: list[Detection]
    ball: Detection | None


NEAR_BALL_HEIGHTS = 2.5
"""Un giocatore è "vicino al pallone" entro questa distanza, in altezze del corpo."""


@dataclass(frozen=True)
class Activity:
    score: float
    matched: int
    ball_speed: float | None
    top_speed: float = 0.0
    """Velocità del giocatore più rapido, in altezze/s."""
    near_ball: int = 0
    """Giocatori vicini al pallone nel campione corrente (0 se il pallone non è visto)."""
    spread: float = 0.0
    """Dispersione dei giocatori in campo, in altezze: alta = squadre allungate, bassa = raccolte."""


def describe(state: FieldState) -> tuple[int, float]:
    """Giocatori vicini al pallone e dispersione dei giocatori nel campione."""
    if not state.players:
        return 0, 0.0
    feet = np.array([p.foot for p in state.players], dtype=np.float64)
    reference = max(float(np.median([p.height for p in state.players])), 1.0)
    spread = float(np.sqrt(feet.var(axis=0).sum())) / reference if len(feet) > 1 else 0.0
    near = 0
    if state.ball is not None:
        distances = np.linalg.norm(feet - np.array(state.ball.center), axis=1) / reference
        near = int((distances <= NEAR_BALL_HEIGHTS).sum())
    return near, spread


def field_state(second: float, detections: list[Detection], pitch: np.ndarray, frame_height: int) -> FieldState:
    """Tiene solo chi ha i piedi sul campo (esclude pubblico, panchine, steward) e il pallone più probabile in campo."""
    def on_pitch(x: float, y: float) -> bool:
        row = int(min(pitch.shape[0] - 1, max(0, y - 1)))
        col = int(min(pitch.shape[1] - 1, max(0, x)))
        return pitch[row, col] > 0

    players = [d for d in detections if d.label == PERSON
               and d.height < MAX_PLAYER_HEIGHT_RATIO * frame_height and on_pitch(*d.foot)]
    balls = [d for d in detections if d.label == BALL and d.confidence >= MIN_BALL_CONFIDENCE and on_pitch(*d.center)]
    if players:
        reference = float(np.median([p.height for p in players]))
        low, high = BALL_SIZE_RANGE
        balls = [b for b in balls if low * reference <= max(b.x2 - b.x1, b.y2 - b.y1) <= high * reference]
    else:
        balls = []  # senza giocatori non c'è scala per giudicare se è davvero un pallone
    return FieldState(second, players, max(balls, key=lambda d: d.confidence, default=None))


def activity(previous: FieldState, current: FieldState, camera: np.ndarray | None) -> Activity:
    """camera: affine 2x3 da previous a current in coordinate del fotogramma originale (None = camera ferma)."""
    dt = current.second - previous.second
    near_ball, spread = describe(current)
    if dt <= 0 or not previous.players or not current.players:
        return Activity(0.0, 0, None, near_ball=near_ball, spread=spread)

    before = _warp([p.foot for p in previous.players], camera)
    after = np.array([p.foot for p in current.players], dtype=np.float64)
    heights_before = np.array([p.height for p in previous.players])
    heights_after = np.array([p.height for p in current.players])

    # abbinamento greedy per distanza crescente: con campioni ogni 0,5 s basta e non serve scipy
    distances = np.linalg.norm(before[:, None, :] - after[None, :, :], axis=2)
    scale = (heights_before[:, None] + heights_after[None, :]) / 2
    relative = distances / np.maximum(scale, 1.0)
    speeds: list[float] = []
    used_before: set[int] = set()
    used_after: set[int] = set()
    for flat in np.argsort(relative, axis=None):
        i, j = np.unravel_index(flat, relative.shape)
        if relative[i, j] > MAX_MATCH_HEIGHTS:
            break
        if i in used_before or j in used_after:
            continue
        used_before.add(i)
        used_after.add(j)
        speed = relative[i, j] / dt
        if speed <= MAX_PLAYER_SPEED:
            speeds.append(float(speed))

    top = sorted(speeds, reverse=True)[:TOP_PLAYERS]
    score = float(np.mean(top)) if top else 0.0

    ball_speed = None
    if previous.ball and current.ball:
        reference = float(np.median(heights_after))
        moved = np.linalg.norm(_warp([previous.ball.center], camera)[0] - np.array(current.ball.center))
        ball_speed = min(MAX_BALL_SPEED, moved / max(reference, 1.0) / dt)
        score += BALL_WEIGHT * ball_speed
    return Activity(score, len(speeds), ball_speed, max(speeds, default=0.0), near_ball, spread)


def scale_transform(matrix: np.ndarray | None, small_size: tuple[int, int],
                    full_size: tuple[int, int]) -> np.ndarray | None:
    """Porta un'affine stimata sul fotogramma ridotto (w, h) alle coordinate del fotogramma originale."""
    if matrix is None:
        return None
    sx, sy = small_size[0] / full_size[0], small_size[1] / full_size[1]
    to_small = np.diag([sx, sy, 1.0])
    to_full = np.diag([1 / sx, 1 / sy, 1.0])
    full = to_full @ np.vstack([matrix, [0, 0, 1]]) @ to_small
    return full[:2]


def _warp(points: list[tuple[float, float]], camera: np.ndarray | None) -> np.ndarray:
    array = np.array(points, dtype=np.float64).reshape(-1, 2)
    if camera is None:
        return array
    return array @ camera[:, :2].T + camera[:, 2]
