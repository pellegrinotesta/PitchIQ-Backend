import unittest

import numpy as np

from app.detection import BALL, PERSON, Detection, MODEL_PATH
from app.player_activity import FieldState, activity, field_state, scale_transform


def player(x, foot_y, height=40.0, confidence=0.9):
    return Detection(PERSON, confidence, x - height / 6, foot_y - height, x + height / 6, foot_y)


def ball(x, y, size=5.0, confidence=0.6):
    return Detection(BALL, confidence, x - size / 2, y - size / 2, x + size / 2, y + size / 2)


class ActivityTest(unittest.TestCase):
    def test_speed_is_measured_in_body_heights_per_second(self):
        before = FieldState(0.0, [player(100, 200)], None)
        after = FieldState(0.5, [player(140, 200)], None)  # 40 px = 1 altezza in 0,5 s

        self.assertAlmostEqual(activity(before, after, None).score, 2.0, places=3)

    def test_camera_pan_is_removed_before_measuring(self):
        before = FieldState(0.0, [player(100, 200), player(300, 220)], None)
        # la camera si sposta: tutto scorre di -60 px, ma i giocatori sono fermi
        after = FieldState(0.5, [player(40, 200), player(240, 220)], None)
        pan = np.float64([[1, 0, -60], [0, 1, 0]])

        self.assertGreater(activity(before, after, None).score, 2.5)
        self.assertAlmostEqual(activity(before, after, pan).score, 0.0, places=3)

    def test_ball_speed_adds_to_the_score(self):
        before = FieldState(0.0, [player(100, 200)], ball(150, 190))
        after = FieldState(0.5, [player(100, 200)], ball(350, 190))  # 200 px = 5 altezze in 0,5 s

        result = activity(before, after, None)

        self.assertAlmostEqual(result.ball_speed, 10.0, places=3)
        self.assertAlmostEqual(result.score, 3.0, places=3)

    def test_unmatched_far_detections_do_not_count_as_speed(self):
        before = FieldState(0.0, [player(100, 200)], None)
        after = FieldState(0.5, [player(600, 200)], None)  # 12 altezze: non è lo stesso giocatore

        self.assertEqual(activity(before, after, None).score, 0.0)


class FieldStateTest(unittest.TestCase):
    def setUp(self):
        self.pitch = np.zeros((360, 640), np.uint8)
        self.pitch[150:300] = 255  # campo nella fascia centrale

    def test_keeps_only_people_standing_on_the_pitch(self):
        detections = [
            player(100, 250),                 # giocatore
            player(200, 120),                 # spettatore in tribuna
            player(300, 359, height=200),     # testa in primo piano, troppo grande
        ]

        state = field_state(1.0, detections, self.pitch, 360)

        self.assertEqual([p.foot for p in state.players], [(100.0, 250)])

    def test_ball_must_have_a_plausible_size(self):
        players = [player(100, 250), player(300, 260)]
        real = ball(200, 240, size=5)
        floodlight = ball(400, 200, size=30, confidence=0.9)  # troppo grande rispetto ai giocatori

        state = field_state(1.0, players + [real, floodlight], self.pitch, 360)

        self.assertEqual(state.ball, real)


class ScaleTransformTest(unittest.TestCase):
    def test_translation_is_scaled_to_full_resolution(self):
        small = np.float64([[1, 0, 10], [0, 1, 5]])

        full = scale_transform(small, (320, 180), (1920, 1080))

        np.testing.assert_allclose(full, [[1, 0, 60], [0, 1, 30]])


@unittest.skipUnless(MODEL_PATH.is_file(), "modello non scaricato: python tools/download_models.py")
class DetectorSmokeTest(unittest.TestCase):
    def test_model_loads_and_runs_on_an_empty_frame(self):
        from app.detection import Detector

        detector = Detector.shared()

        self.assertIsNotNone(detector)
        self.assertEqual(detector.detect(np.zeros((360, 640, 3), np.uint8)), [])


if __name__ == "__main__":
    unittest.main()
