import tempfile
import unittest
from pathlib import Path

from app.clip_features import FEATURE_NAMES, SampleDetail, clip_features, clues
from app.event_model import EventModel


def features(**values):
    base = {name: 0.0 for name in FEATURE_NAMES}
    base["metodo_giocatori"] = 1.0
    base.update(values)
    return base


def shot(i):
    return {"etichetta": "TIRO", "caratteristiche": features(velocita_max_palla=18 + i, giocatori_vicini_palla_max=1)}


def tackle(i):
    return {"etichetta": "CONTRASTO",
            "caratteristiche": features(velocita_max_palla=2 + i * 0.1, giocatori_vicini_palla_max=4)}


class EventModelTest(unittest.TestCase):
    def test_inactive_until_two_classes_have_enough_examples(self):
        model = EventModel.train([shot(0), shot(1), shot(2), tackle(0), tackle(1)])

        self.assertFalse(model.active)
        self.assertIsNone(model.propose(features(velocita_max_palla=20)))
        self.assertIsNone(model.estimated_accuracy())

    def test_proposes_the_nearest_class_with_confidence(self):
        model = EventModel.train([shot(i) for i in range(4)] + [tackle(i) for i in range(4)])

        proposal = model.propose(features(velocita_max_palla=19, giocatori_vicini_palla_max=1))

        self.assertTrue(model.active)
        self.assertEqual(proposal.label, "TIRO")
        self.assertGreaterEqual(proposal.confidence, 0.6)
        self.assertEqual(model.estimated_accuracy(), 1.0)

    def test_ignores_unknown_labels_and_round_trips_to_disk(self):
        examples = [shot(i) for i in range(3)] + [tackle(i) for i in range(3)] + [
            {"etichetta": "BOH", "caratteristiche": features()}]
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "model.json"
            EventModel.train(examples).save(path, examples)

            loaded = EventModel.load(path)

        self.assertEqual(loaded.summary()["esempi"], 6)
        self.assertEqual(loaded.summary()["perClasse"]["TIRO"], 3)


class ClipFeaturesTest(unittest.TestCase):
    def test_features_and_clues_from_samples(self):
        details = [
            SampleDetail(1.0, 0.5, top_speed=0.5, players=10, spread=6),
            SampleDetail(1.5, 0.6, top_speed=0.8, players=10, spread=6),
            SampleDetail(2.0, 6.0, top_speed=4.5, ball_speed=20, near_ball=1, players=10, spread=9),
        ]

        result = clip_features(details, 1.0, 2.0, with_players=True)

        self.assertEqual(result["velocita_max_palla"], 20)
        self.assertEqual(result["variazione_dispersione"], 3)
        self.assertEqual(set(result), set(FEATURE_NAMES))
        self.assertIn("Pallone calciato forte", clues(result))
        self.assertIn("Scatto di almeno un giocatore", clues(result))
        self.assertIn("Da fermo a improvvisa accelerazione", clues(result))

    def test_no_clues_without_player_detection(self):
        self.assertEqual(clues(clip_features([SampleDetail(1.0, 9.0)], 0, 2, with_players=False)), [])


if __name__ == "__main__":
    unittest.main()
