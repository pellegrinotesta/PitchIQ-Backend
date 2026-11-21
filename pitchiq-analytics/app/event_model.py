"""Classificatore degli eventi addestrato sulle etichette date dall'utente alle clip.

k-NN pesato sulle caratteristiche standardizzate: con poche decine di esempi è più affidabile
di un modello con parametri da imparare, si "riaddestra" all'istante e la sua accuratezza si
stima onestamente con il leave-one-out (ogni esempio classificato dagli altri).
"""
from __future__ import annotations

import json
import os
from collections import Counter
from dataclasses import dataclass
from pathlib import Path

import numpy as np

from .clip_features import FEATURE_NAMES, vector

LABELS = ("TIRO", "PASSAGGIO_LUNGO", "CONTRASTO", "RIPARTENZA", "CALCIO_PIAZZATO", "NIENTE")
MIN_PER_CLASS = 3
MIN_CLASSES = 2
MAX_K = 5
MIN_CONFIDENCE = 0.6
"""Sotto questa quota di voti la proposta non viene mostrata: meglio nessuna che una a caso."""


@dataclass(frozen=True)
class Proposal:
    label: str
    confidence: float


class EventModel:
    def __init__(self, features: np.ndarray, labels: list[str]) -> None:
        self.labels = labels
        self.mean = features.mean(axis=0) if len(features) else np.zeros(len(FEATURE_NAMES))
        std = features.std(axis=0) if len(features) else np.ones(len(FEATURE_NAMES))
        self.std = np.where(std > 1e-9, std, 1.0)  # caratteristiche costanti non pesano
        self.points = (features - self.mean) / self.std if len(features) else features

    @classmethod
    def train(cls, examples: list[dict]) -> EventModel:
        valid = [e for e in examples if e.get("etichetta") in LABELS and isinstance(e.get("caratteristiche"), dict)]
        features = np.array([vector(e["caratteristiche"]) for e in valid]).reshape(-1, len(FEATURE_NAMES))
        return cls(features, [e["etichetta"] for e in valid])

    # ---------- stato ----------

    def per_class(self) -> dict[str, int]:
        counts = Counter(self.labels)
        return {label: counts.get(label, 0) for label in LABELS}

    def usable_classes(self) -> list[str]:
        return [label for label, n in self.per_class().items() if n >= MIN_PER_CLASS]

    @property
    def active(self) -> bool:
        return len(self.usable_classes()) >= MIN_CLASSES

    def estimated_accuracy(self) -> float | None:
        """Leave-one-out sugli esempi delle classi utilizzabili."""
        usable = set(self.usable_classes())
        if len(usable) < MIN_CLASSES:
            return None
        indices = [i for i, label in enumerate(self.labels) if label in usable]
        correct = 0
        for i in indices:
            others = [j for j in indices if j != i]
            vote = self._vote(self.points[i], others)
            correct += vote is not None and vote.label == self.labels[i]
        return correct / len(indices)

    def summary(self) -> dict:
        accuracy = self.estimated_accuracy()
        return {
            "esempi": len(self.labels),
            "perClasse": self.per_class(),
            "attivo": self.active,
            "accuratezzaStimata": round(accuracy, 3) if accuracy is not None else None,
            "minimoPerClasse": MIN_PER_CLASS,
        }

    # ---------- previsione ----------

    def propose(self, features: dict[str, float]) -> Proposal | None:
        if not self.active:
            return None
        usable = set(self.usable_classes())
        point = (vector(features) - self.mean) / self.std
        vote = self._vote(point, [i for i, label in enumerate(self.labels) if label in usable])
        if vote is None or vote.confidence < MIN_CONFIDENCE:
            return None
        return vote

    def _vote(self, point: np.ndarray, indices: list[int]) -> Proposal | None:
        if not indices:
            return None
        distances = np.linalg.norm(self.points[indices] - point, axis=1)
        k = min(MAX_K, len(indices))
        nearest = np.argsort(distances)[:k]
        weights: Counter[str] = Counter()
        for n in nearest:
            weights[self.labels[indices[n]]] += 1.0 / (distances[n] + 1e-6)
        label, weight = weights.most_common(1)[0]
        return Proposal(label, weight / sum(weights.values()))

    # ---------- persistenza ----------

    def save(self, path: Path, examples: list[dict]) -> None:
        temporary = path.with_suffix(".tmp")
        temporary.write_text(json.dumps({"esempi": examples}, ensure_ascii=False), encoding="utf-8")
        os.replace(temporary, path)

    @classmethod
    def load(cls, path: Path) -> EventModel:
        if not path.is_file():
            return cls.train([])
        try:
            return cls.train(json.loads(path.read_text(encoding="utf-8")).get("esempi", []))
        except (OSError, ValueError):
            return cls.train([])
