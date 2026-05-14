package me.zamin.anchor.internal.validation;

import java.util.Objects;

public record ValidationIssue(
    ValidationSeverity severity,
    ValidationCategory category,
    String problem,
    String cause,
    String recommendedFix
) {
    public ValidationIssue {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(cause, "cause");
        Objects.requireNonNull(recommendedFix, "recommendedFix");
    }
}
