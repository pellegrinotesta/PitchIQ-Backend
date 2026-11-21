"""Caratteristiche di una clip per il classificatore di eventi, e indizi leggibili per l'utente.

Le caratteristiche sono pensate per distinguere i tipi di azione senza guardare i pixel:
- tiro / passaggio lungo: pallone molto veloce;
- contrasto: più giocatori stretti attorno al pallone, scatti brevi;
- ripartenza: giocatori veloci, squadre che si allungano, camera che segue un'azione rapida;
- calcio piazzato: giocatori raccolti e quasi fermi prima, poi pallone veloce.
"""
from __future__ import annotations

from dataclasses import dataclass

import numpy as np

FEATURE_NAMES = (
    "durata",
    "attivita_max",
    "attivita_media",
    "velocita_max_giocatore",
    "palla_visibile",
    "velocita_max_palla",
    "giocatori_vicini_palla_max",
    "dispersione_media",
    "variazione_dispersione",
    "attivita_iniziale",
    "movimento_camera_medio",
    "giocatori_medi",
    "metodo_giocatori",
)


@dataclass(frozen=True)
class SampleDetail:
    second: float
    score: float
    top_speed: float = 0.0
    ball_speed: float | None = None
    near_ball: int = 0
    spread: float = 0.0
    players: int = 0
    camera: float = 0.0
    """Spostamento della camera nel campione, come frazione della larghezza del fotogramma."""


def clip_features(details: list[SampleDetail], start: float, end: float, with_players: bool) -> dict[str, float]:
    window = [d for d in details if start <= d.second <= end] or details[:1]
    if not window:
        return {name: 0.0 for name in FEATURE_NAMES}
    scores = np.array([d.score for d in window])
    spreads = np.array([d.spread for d in window if d.players > 1] or [0.0])
    third = max(1, len(window) // 3)
    ball_speeds = [d.ball_speed for d in window if d.ball_speed is not None]
    return {
        "durata": round(end - start, 3),
        "attivita_max": round(float(scores.max()), 3),
        "attivita_media": round(float(scores.mean()), 3),
        "velocita_max_giocatore": round(max(d.top_speed for d in window), 3),
        "palla_visibile": round(sum(d.ball_speed is not None or d.near_ball > 0 for d in window) / len(window), 3),
        "velocita_max_palla": round(max(ball_speeds, default=0.0), 3),
        "giocatori_vicini_palla_max": float(max(d.near_ball for d in window)),
        "dispersione_media": round(float(spreads.mean()), 3),
        "variazione_dispersione": round(float(spreads[-1] - spreads[0]), 3),
        "attivita_iniziale": round(float(scores[:third].mean()), 3),
        "movimento_camera_medio": round(float(np.mean([d.camera for d in window])), 4),
        "giocatori_medi": round(float(np.mean([d.players for d in window])), 2),
        "metodo_giocatori": 1.0 if with_players else 0.0,
    }


def clues(features: dict[str, float]) -> list[str]:
    """Indizi trasparenti, mostrati all'utente al posto di un nome di evento finché il modello non è pronto."""
    if not features.get("metodo_giocatori"):
        return []
    found: list[str] = []
    if features["velocita_max_palla"] >= 12:
        found.append("Pallone calciato forte")
    if features["giocatori_vicini_palla_max"] >= 3:
        found.append("Più giocatori stretti sul pallone")
    if features["velocita_max_giocatore"] >= 4:
        found.append("Scatto di almeno un giocatore")
    if features["movimento_camera_medio"] >= 0.08 and features["attivita_media"] >= 2:
        found.append("La camera segue un'azione veloce")
    if features["variazione_dispersione"] >= 1.5:
        found.append("Le squadre si allungano")
    elif features["variazione_dispersione"] <= -1.5:
        found.append("I giocatori si raccolgono")
    if features["attivita_iniziale"] <= 1 and features["attivita_max"] >= 3:
        found.append("Da fermo a improvvisa accelerazione")
    return found


def vector(features: dict[str, float]) -> np.ndarray:
    return np.array([float(features.get(name, 0.0)) for name in FEATURE_NAMES], dtype=np.float64)
