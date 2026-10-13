package com.pdffixnow.desktop.model;

import java.util.List;

/** Immutable result of validating one file (produced on a worker thread). */
public record Outcome(
        String status,          // Valid, Invalid, Error, Cancelled
        String detectedProfile, // e.g. "PDF/A-2b" or "-"
        String message,
        int errorCount,
        long millis,
        List<RuleFailure> failures) {

    public static Outcome error(String message, long millis) {
        return new Outcome(FileResult.ERROR, "-", message, 0, millis, List.of());
    }

    public static Outcome cancelled() {
        return new Outcome(FileResult.CANCELLED, "-", "Cancelled", 0, 0, List.of());
    }
}
