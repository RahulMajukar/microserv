package com.learn.revenue.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueResponse(
        Long id,
        String source,
        String category,
        BigDecimal amount,
        String currency,
        LocalDate recordedOn,
        String note
) {
}
