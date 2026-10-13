package com.pdffixnow.desktop.model;

/** One failed PDF/A rule check. */
public record RuleFailure(String clause, String testNumber, String message, String context) {}
