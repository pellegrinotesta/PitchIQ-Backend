"""Segmentazione deterministica di intervalli con movimento sopra soglia."""
from __future__ import annotations

from dataclasses import dataclass
from math import ceil
from statistics import median

CLIP_PADDING_SECONDS = 2.0
MAX_CANDIDATES = 10


@dataclass(frozen=True)
class MotionSample:
    second: float
    score: float


def motion_threshold(samples: list[MotionSample]) -> float | None:
    """Soglia oltre la quale un campione conta come picco; None se i campioni sono troppo pochi."""
    if len(samples) < 3:
        return None
    scores = [sample.score for sample in samples]
    center = median(scores)
    mad = median([abs(score - center) for score in scores])
    percentile_80 = sorted(scores)[ceil((len(scores) - 1) * 0.8)]
    # soglia relativa al video: con la camera che segue il gioco il movimento di fondo è alto,
    # quindi cerchiamo i picchi rispetto alla mediana invece di uno scarto assoluto elevato
    return max(center + max(1.0, 1.5 * mad), percentile_80)


def candidate_intervals(samples: list[MotionSample], duration: float) -> list[tuple[float, float, float]]:
    threshold = motion_threshold(samples)
    if threshold is None:
        return []

    active = [sample.score >= threshold and sample.score > 0 for sample in samples]

    groups: list[list[int]] = []
    for index, is_active in enumerate(active):
        if not is_active:
            continue
        if groups and samples[index].second - samples[groups[-1][-1]].second <= 1.5:
            groups[-1].append(index)
        else:
            groups.append([index])

    candidates = []
    for group in groups:
        start = max(0.0, samples[group[0]].second - CLIP_PADDING_SECONDS)
        end = min(duration, samples[group[-1]].second + CLIP_PADDING_SECONDS)
        if end - start < 1:
            continue
        intensity = max(samples[index].score for index in group)
        if candidates and start <= candidates[-1][1]:
            # i margini di due picchi vicini si sovrappongono: un'unica clip
            prev_start, prev_end, prev_intensity = candidates[-1]
            candidates[-1] = (prev_start, max(prev_end, end), max(prev_intensity, intensity))
        else:
            candidates.append((start, end, intensity))

    strongest = sorted(candidates, key=lambda item: item[2], reverse=True)[:MAX_CANDIDATES]
    return sorted(strongest)
