package com.learn.common.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The JSON contract that crosses the network.
 * revenue-service builds it from its own table.
 * report-service deserializes it. It is not stored as a shared table.
 */
public record RevenueSummary(
        LocalDate from,
        LocalDate to,
        BigDecimal totalAmount,
        String currency,
        List<CategoryAmount> byCategory
) {
}
