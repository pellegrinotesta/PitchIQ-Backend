"""Scarica il modello di riconoscimento giocatori/pallone (YOLOX-Tiny, Apache-2.0) e ne verifica l'integrità.

Uso:  python tools/download_models.py
Il file finisce in models/ (non versionato). Senza modello il servizio funziona lo stesso,
ripiegando sull'analisi del movimento dei pixel.
"""
from __future__ import annotations

import hashlib
import sys
import urllib.request
from pathlib import Path

MODEL = {
    "name": "yolox_tiny.onnx",
    "url": "https://github.com/Megvii-BaseDetection/YOLOX/releases/download/0.1.1rc0/yolox_tiny.onnx",
    "sha256": "427cc366d34e27ff7a03e2899b5e3671425c262ea2291f88bb942bc1cc70b0f7",
}
TARGET_DIR = Path(__file__).resolve().parent.parent / "models"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 20), b""):
            digest.update(chunk)
    return digest.hexdigest()


def main() -> int:
    TARGET_DIR.mkdir(exist_ok=True)
    target = TARGET_DIR / MODEL["name"]
    if target.is_file() and sha256(target) == MODEL["sha256"]:
        print(f"{target} già presente e verificato.")
        return 0
    partial = target.with_suffix(".part")
    print(f"Scarico {MODEL['url']} ...")
    urllib.request.urlretrieve(MODEL["url"], partial)
    if sha256(partial) != MODEL["sha256"]:
        partial.unlink(missing_ok=True)
        print("Checksum non valido: download scartato.", file=sys.stderr)
        return 1
    partial.replace(target)
    print(f"Modello salvato in {target}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
