package com.aitutor.app.util;

public final class ScoreClassifier {

    private static final double MASTERY_THRESHOLD = 70.0;
    private static final double WEAK_THRESHOLD = 60.0;

    private ScoreClassifier() {}

    public enum Status { MASTERED, IN_PROGRESS, WEAK }

    public static Status classify(double score) {
        if (score >= MASTERY_THRESHOLD) return Status.MASTERED;
        if (score < WEAK_THRESHOLD) return Status.WEAK;
        return Status.IN_PROGRESS;
    }
}
