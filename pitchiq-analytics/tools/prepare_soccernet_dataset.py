"""Download (when authorized) and normalize SoccerNet Ball Action Spotting labels."""
from __future__ import annotations

import argparse
import getpass
import json
import sys
import zipfile
from collections import Counter
from pathlib import Path

SOURCE_TASK = "spotting-ball-2024"
DOWNLOAD_SPLITS = ("train", "valid", "test")
LABEL_FILE = "Labels-ball.json"

# DRIVE is kept distinct: it is related to ball carrying, but is not a verified
# synonym for every dribbling event. Interceptions are not in the source taxonomy.
LABEL_MAP = {
    "PASS": ("PASSAGGIO", "exact"),
    "SHOT": ("TIRO", "exact"),
    "GOAL": ("GOAL", "exact"),
    "PLAYER SUCCESSFUL TACKLE": ("CONTRASTO", "exact"),
    "DRIVE": ("DRIVE", "related_not_equivalent"),
}
TARGET_CLASSES = ("PASSAGGIO", "TIRO", "DRIBBLING", "CONTRASTO", "INTERCETTO", "GOAL")


class DatasetError(ValueError):
    """The downloaded dataset is incomplete or has an invalid annotation."""


def _safe_extract(archive: zipfile.ZipFile, destination: Path) -> None:
    root = destination.resolve()
    for member in archive.infolist():
        target = (root / member.filename).resolve()
        if target != root and root not in target.parents:
            raise DatasetError(f"Percorso non sicuro nell'archivio: {member.filename}")
    archive.extractall(root)


def download_authorized_dataset(destination: Path) -> None:
    try:
        from SoccerNet.Downloader import SoccerNetDownloader
    except ImportError as exc:
        raise DatasetError(
            "Per il download installa SoccerNet con `python -m pip install SoccerNet`."
        ) from exc

    password = getpass.getpass(
        "Password SoccerNet ottenuta dopo la firma dell'NDA (non viene salvata): "
    )
    if not password:
        raise DatasetError("Password non fornita; download annullato.")

    destination.mkdir(parents=True, exist_ok=True)
    downloader = SoccerNetDownloader(LocalDirectory=str(destination))
    downloader.downloadDataTask(
        task=SOURCE_TASK,
        split=list(DOWNLOAD_SPLITS),
        password=password,
    )

    task_dir = destination / SOURCE_TASK
    for split in DOWNLOAD_SPLITS:
        archive_path = task_dir / f"{split}.zip"
        if not archive_path.is_file():
            raise DatasetError(f"Archivio atteso non trovato: {archive_path}")
        with zipfile.ZipFile(archive_path) as archive:
            _safe_extract(archive, task_dir)
        archive_path.unlink()


def normalize_dataset(source: Path, output: Path) -> dict:
    source = source.resolve()
    output.mkdir(parents=True, exist_ok=True)
    label_files = sorted(source.rglob(LABEL_FILE))
    if not label_files:
        raise DatasetError(f"Nessun {LABEL_FILE} trovato in {source}.")

    manifest_path = output / "annotations.jsonl"
    counts: Counter[str] = Counter()
    quality_counts: Counter[str] = Counter()
    unmapped_counts: Counter[str] = Counter()
    games: set[str] = set()
    event_count = 0

    with manifest_path.open("w", encoding="utf-8", newline="\n") as manifest:
        for labels_path in label_files:
            try:
                data = json.loads(labels_path.read_text(encoding="utf-8"))
            except (OSError, json.JSONDecodeError) as exc:
                raise DatasetError(f"Annotazioni non leggibili: {labels_path}") from exc
            annotations = data.get("annotations")
            if not isinstance(annotations, list):
                raise DatasetError(f"Campo 'annotations' assente o non valido: {labels_path}")

            game_id = labels_path.parent.relative_to(source).as_posix()
            games.add(game_id)
            for index, annotation in enumerate(annotations):
                if not isinstance(annotation, dict):
                    raise DatasetError(f"Annotazione {index} non valida in {labels_path}.")
                source_label = str(annotation.get("label", "")).strip().upper()
                mapped = LABEL_MAP.get(source_label)
                if mapped is None:
                    unmapped_counts[source_label or "(vuota)"] += 1
                    continue

                try:
                    position_ms = float(annotation["position"])
                    half = int(str(annotation["gameTime"]).split(" - ", 1)[0])
                except (KeyError, TypeError, ValueError) as exc:
                    raise DatasetError(
                        f"Timestamp o tempo di gioco non valido in {labels_path}, annotazione {index}."
                    ) from exc
                if position_ms < 0 or half not in (1, 2):
                    raise DatasetError(
                        f"Posizione o tempo non valido in {labels_path}, annotazione {index}."
                    )

                video_candidates = [
                    labels_path.parent / f"{resolution}.mp4"
                    for resolution in ("224p", "720p")
                ]
                video_path = next((path for path in video_candidates if path.is_file()), None)
                if video_path is None:
                    raise DatasetError(
                        f"Video 224p.mp4/720p.mp4 mancante accanto a {labels_path}."
                    )
                canonical_label, quality = mapped
                record = {
                    "eventId": f"{game_id}:half-{half}:{index}",
                    "gameId": game_id,
                    "half": half,
                    "videoPath": video_path.relative_to(source).as_posix(),
                    "timestampSeconds": round(position_ms / 1000, 3),
                    "label": canonical_label,
                    "sourceLabel": source_label,
                    "labelQuality": quality,
                    "source": "SoccerNet-Ball-Action-Spotting-2024",
                    "camera": "broadcast",
                }
                manifest.write(json.dumps(record, ensure_ascii=False) + "\n")
                counts[canonical_label] += 1
                quality_counts[quality] += 1
                event_count += 1

    missing_classes = [label for label in TARGET_CLASSES if counts[label] == 0]
    summary = {
        "source": "SoccerNet Ball Action Spotting 2024",
        "sourceUrl": "https://github.com/SoccerNet/sn-spotting",
        "annotationCount": event_count,
        "gameCount": len(games),
        "camera": "broadcast",
        "targetClasses": list(TARGET_CLASSES),
        "classCounts": dict(sorted(counts.items())),
        "labelQualityCounts": dict(sorted(quality_counts.items())),
        "unmappedSourceLabelCounts": dict(sorted(unmapped_counts.items())),
        "classesWithoutUsableLabels": missing_classes,
        "manifest": manifest_path.name,
        "videoFilesCopied": False,
    }
    (output / "summary.json").write_text(
        json.dumps(summary, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    return summary


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, required=True,
                        help="Directory containing extracted SoccerNet Ball Action files.")
    parser.add_argument("--output", type=Path, required=True,
                        help="Directory for normalized annotations and coverage summary.")
    parser.add_argument("--download", action="store_true",
                        help="Download official train/valid/test files using an NDA password.")
    args = parser.parse_args(argv)

    try:
        if args.download:
            download_authorized_dataset(args.source)
        summary = normalize_dataset(args.source, args.output)
    except (DatasetError, OSError, zipfile.BadZipFile) as exc:
        print(f"Errore preparazione dataset: {exc}", file=sys.stderr)
        return 1

    print(json.dumps(summary, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
