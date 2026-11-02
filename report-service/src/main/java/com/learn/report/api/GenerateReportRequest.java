package com.learn.report.api;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GenerateReportRequest(
        @NotNull LocalDate from,
        @NotNull LocalDate to
) {
}
