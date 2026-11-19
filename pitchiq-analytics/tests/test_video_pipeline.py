import unittest

from app.video_segments import MotionSample, candidate_intervals


class CandidateIntervalsTest(unittest.TestCase):
    def test_returns_padded_intervals_for_motion_peaks(self):
        samples = [
            MotionSample(second=second * 0.5, score=score)
            for second, score in enumerate([1, 1, 1, 30, 1, 1, 1, 1, 35, 1, 1, 1])
        ]

        candidates = candidate_intervals(samples, duration=6)

        self.assertEqual(len(candidates), 2)
        self.assertEqual(candidates[0], (0.0, 3.5, 30))
        self.assertEqual(candidates[1], (2.0, 6.0, 35))

    def test_returns_no_candidates_for_uniform_motion(self):
        samples = [MotionSample(second=i * 0.5, score=12) for i in range(12)]

        self.assertEqual(candidate_intervals(samples, duration=6), [])


if __name__ == "__main__":
    unittest.main()
