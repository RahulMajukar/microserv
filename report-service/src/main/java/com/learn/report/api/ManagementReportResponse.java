package com.learn.report.api;

import com.learn.common.api.CategoryAmount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ManagementReportResponse(
        Long id,
        LocalDate periodFrom,
        LocalDate periodTo,
        BigDecimal totalRevenue,
        String currency,
        List<CategoryAmount> byCategory,
        String narrative,
        LocalDateTime createdAt
) {
}
