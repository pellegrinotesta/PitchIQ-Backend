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


def candidate_intervals(samples: list[MotionSample], duration: float) -> list[tuple[float, float, float]]:
    if len(samples) < 3:
        return []

    scores = [sample.score for sample in samples]
    center = median(scores)
    mad = median([abs(score - center) for score in scores])
    percentile_75 = sorted(scores)[ceil((len(scores) - 1) * 0.75)]
    threshold = max(center + max(5.0, 2.5 * mad), percentile_75)
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
        candidates.append((start, end, intensity))

    strongest = sorted(candidates, key=lambda item: item[2], reverse=True)[:MAX_CANDIDATES]
    return sorted(strongest)
