package me.zamin.anchor.internal.validation;

import java.util.List;
import java.util.Objects;

public record ValidationReport(
    long generatedAtMillis,
    List<ValidationIssue> issues
) {
    public ValidationReport {
        Objects.requireNonNull(issues, "issues");
        issues = List.copyOf(issues);
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(issue -> issue.severity() == ValidationSeverity.ERROR);
    }

    public boolean hasWarnings() {
        return issues.stream().anyMatch(issue -> issue.severity() == ValidationSeverity.WARNING);
    }
}
