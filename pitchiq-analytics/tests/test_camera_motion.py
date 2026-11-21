import unittest

import cv2
import numpy as np

from app.camera_motion import frame_difference, pitch_mask


def _scene(width=420, height=260, seed=7):
    """Sfondo con dettagli (come tribune e linee del campo) più grande del fotogramma, per simulare la panoramica."""
    rng = np.random.default_rng(seed)
    img = cv2.GaussianBlur((rng.random((height, width)) * 255).astype(np.uint8), (0, 0), 2)
    for x in range(0, width, 40):
        cv2.line(img, (x, 0), (x, height), 255, 2)
    return img


def _crop(scene, dx, dy=0):
    return scene[40 + dy:220 + dy, 40 + dx:360 + dx].copy()  # 320x180


class FrameDifferenceTest(unittest.TestCase):
    def test_pure_camera_pan_is_almost_cancelled(self):
        scene = _scene()
        previous, current = _crop(scene, 0), _crop(scene, 25)

        result = frame_difference(previous, current)
        raw = float(cv2.absdiff(previous, current).mean())

        self.assertTrue(result.compensated)
        self.assertAlmostEqual(result.camera_shift, 25, delta=2)
        self.assertLess(result.score, raw * 0.2)

    def test_player_movement_survives_the_compensation(self):
        scene = _scene()
        previous, current = _crop(scene, 0), _crop(scene, 25)
        cv2.circle(previous, (150, 90), 12, 0, -1)
        cv2.circle(current, (190, 100), 12, 0, -1)  # giocatore che si sposta mentre la camera segue

        still = frame_difference(_crop(scene, 0), _crop(scene, 25)).score
        moving = frame_difference(previous, current).score

        self.assertGreater(moving, still * 1.5)

    def test_static_camera_without_motion_scores_near_zero(self):
        frame = _crop(_scene(), 0)

        self.assertLess(frame_difference(frame, frame.copy()).score, 0.5)


class PitchMaskTest(unittest.TestCase):
    def test_motion_outside_the_pitch_is_ignored(self):
        # metà superiore: tribuna grigia con una bandiera che si muove; metà inferiore: campo verde fermo
        previous = np.zeros((180, 320, 3), np.uint8)
        # tribuna con dettagli (seggiolini, persone): dà allo sfondo i punti per stimare la camera ferma
        noise = (np.random.default_rng(3).random((90, 320)) * 120 + 40).astype(np.uint8)
        previous[:90] = cv2.cvtColor(cv2.GaussianBlur(noise, (0, 0), 1.5), cv2.COLOR_GRAY2BGR)
        previous[90:] = (40, 140, 40)
        current = previous.copy()
        cv2.rectangle(previous, (40, 20), (90, 60), (0, 0, 200), -1)
        cv2.rectangle(current, (60, 25), (110, 65), (0, 0, 200), -1)
        gray = [cv2.cvtColor(f, cv2.COLOR_BGR2GRAY) for f in (previous, current)]

        result = frame_difference(gray[0], gray[1], pitch_mask(current))

        self.assertGreater(result.score, 1)
        self.assertLess(result.pitch_score, 0.5)
        self.assertAlmostEqual(result.pitch_fraction, 0.5, delta=0.05)

    def test_players_do_not_punch_holes_in_the_pitch(self):
        frame = np.zeros((180, 320, 3), np.uint8)
        frame[:] = (40, 140, 40)
        cv2.circle(frame, (160, 90), 6, (255, 255, 255), -1)  # giocatore in maglia bianca

        self.assertEqual(pitch_mask(frame)[90, 160], 255)


    def test_hand_in_front_of_the_lens_is_not_pitch_motion(self):
        pitch_frame = np.zeros((180, 320, 3), np.uint8)
        noise = (np.random.default_rng(5).random((60, 320)) * 120 + 40).astype(np.uint8)
        pitch_frame[:60] = cv2.cvtColor(cv2.GaussianBlur(noise, (0, 0), 1.5), cv2.COLOR_GRAY2BGR)
        pitch_frame[60:] = (40, 140, 40)
        with_hand = pitch_frame.copy()
        cv2.ellipse(with_hand, (120, 150), (35, 60), 0, 0, 360, (150, 170, 220), -1)  # mano color pelle sul campo
        gray = [cv2.cvtColor(f, cv2.COLOR_BGR2GRAY) for f in (pitch_frame, with_hand)]

        result = frame_difference(gray[0], gray[1], pitch_mask(with_hand), pitch_mask(pitch_frame))

        self.assertLess(result.pitch_score, 1.0)


if __name__ == "__main__":
    unittest.main()
