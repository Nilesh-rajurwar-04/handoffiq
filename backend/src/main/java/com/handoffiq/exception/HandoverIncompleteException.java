package com.handoffiq.exception;

import java.util.List;

public class HandoverIncompleteException extends RuntimeException {

    private final int readinessScore;
    private final List<String> missingRequirements;

    public HandoverIncompleteException(String message, int readinessScore, List<String> missingRequirements) {
        super(message);
        this.readinessScore = readinessScore;
        this.missingRequirements = missingRequirements;
    }

    public int getReadinessScore() { return readinessScore; }
    public List<String> getMissingRequirements() { return missingRequirements; }
}
