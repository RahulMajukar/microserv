package com.learn.revenue.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateRevenueRequest(
        @NotBlank @Size(max = 100) String source,
        @NotBlank @Size(max = 100) String category,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull LocalDate recordedOn,
        @Size(max = 255) String note
) {
}
