import unittest

from app.video_segments import MotionSample, candidate_intervals, motion_threshold


class CandidateIntervalsTest(unittest.TestCase):
    def test_returns_padded_intervals_for_motion_peaks(self):
        samples = [
            MotionSample(second=second * 0.5, score=score)
            for second, score in enumerate([1, 1, 1, 30] + [1] * 11 + [35, 1, 1, 1, 1])
        ]

        candidates = candidate_intervals(samples, duration=10)

        self.assertEqual(candidates, [(0.0, 3.5, 30), (5.5, 9.5, 35)])

    def test_merges_peaks_whose_padded_intervals_overlap(self):
        samples = [
            MotionSample(second=second * 0.5, score=score)
            for second, score in enumerate([1, 1, 1, 30, 1, 1, 1, 1, 35, 1, 1, 1])
        ]

        self.assertEqual(candidate_intervals(samples, duration=6), [(0.0, 6.0, 35)])

    def test_returns_no_candidates_for_uniform_motion(self):
        samples = [MotionSample(second=i * 0.5, score=12) for i in range(12)]

        self.assertEqual(candidate_intervals(samples, duration=6), [])


class MotionThresholdTest(unittest.TestCase):
    def test_threshold_sits_above_the_typical_motion(self):
        samples = [MotionSample(second=i * 0.5, score=12) for i in range(12)]

        self.assertEqual(motion_threshold(samples), 13.0)

    def test_threshold_is_none_with_too_few_samples(self):
        self.assertIsNone(motion_threshold([MotionSample(0.5, 3), MotionSample(1.0, 4)]))


if __name__ == "__main__":
    unittest.main()
