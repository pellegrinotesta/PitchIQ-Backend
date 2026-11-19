import json
import tempfile
import unittest
from pathlib import Path

from tools.prepare_soccernet_dataset import DatasetError, normalize_dataset


class PrepareSoccerNetDatasetTest(unittest.TestCase):
    def test_normalizes_events_and_reports_missing_classes(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source = root / "source"
            game = source / "england_efl" / "season" / "match-a"
            output = root / "prepared"
            game.mkdir(parents=True)
            (game / "224p.mp4").write_bytes(b"fixture")
            (game / "Labels-ball.json").write_text(json.dumps({
                "annotations": [
                    {"gameTime": "1 - 00:12", "position": "12000", "label": "Pass"},
                    {"gameTime": "2 - 01:04", "position": "64000", "label": "Drive"},
                    {"gameTime": "1 - 02:10", "position": "130000", "label": "Ball Player Block"},
                ]
            }), encoding="utf-8")

            summary = normalize_dataset(source, output)
            records = [json.loads(line) for line in (output / "annotations.jsonl").read_text(
                encoding="utf-8"
            ).splitlines()]

            self.assertEqual(summary["annotationCount"], 2)
            self.assertEqual(records[0]["label"], "PASSAGGIO")
            self.assertEqual(records[0]["timestampSeconds"], 12)
            self.assertEqual(records[1]["label"], "DRIVE")
            self.assertEqual(records[1]["labelQuality"], "related_not_equivalent")
            self.assertIn("INTERCETTO", summary["classesWithoutUsableLabels"])
            self.assertEqual(summary["unmappedSourceLabelCounts"]["BALL PLAYER BLOCK"], 1)

    def test_rejects_missing_halftime_video(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            game = root / "game"
            game.mkdir()
            (game / "Labels-ball.json").write_text(
                json.dumps({"annotations": [
                    {"gameTime": "1 - 00:12", "position": "12000", "label": "Pass"}
                ]}),
                encoding="utf-8",
            )

            with self.assertRaisesRegex(DatasetError, "Video 224p.mp4/720p.mp4 mancante"):
                normalize_dataset(root, root / "prepared")


if __name__ == "__main__":
    unittest.main()
