package com.learn.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.common.api.CategoryAmount;
import com.learn.common.api.RevenueSummary;
import com.learn.common.error.ResourceNotFoundException;
import com.learn.common.web.DateRanges;
import com.learn.report.api.ManagementReportResponse;
import com.learn.report.client.RevenueClient;
import com.learn.report.db.ManagementReportDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManagementReportService {

    private static final Logger log = LoggerFactory.getLogger(ManagementReportService.class);

    private final ManagementReportDao reportDao;
    private final RevenueClient revenueClient;
    private final ObjectMapper objectMapper;

    public ManagementReportService(
            ManagementReportDao reportDao,
            RevenueClient revenueClient,
            ObjectMapper objectMapper) {
        this.reportDao = reportDao;
        this.revenueClient = revenueClient;
        this.objectMapper = objectMapper;
    }

    public ManagementReportResponse generate(LocalDate from, LocalDate to) {
        DateRanges.requireFromNotAfterTo(from, to);
        log.info("Building management report {} to {} using revenue-service", from, to);
        RevenueSummary summary = revenueClient.fetchSummary(from, to);
        long id = reportDao.insert(
                summary.from(),
                summary.to(),
                summary.totalAmount(),
                summary.currency(),
                writeJson(summary.byCategory()),
                narrate(summary),
                LocalDateTime.now(ZoneOffset.UTC));
        ManagementReportResponse saved = get(id);
        log.info("Stored management report id={} total={} {}", saved.id(), saved.totalRevenue(), saved.currency());
        return saved;
    }

    public List<ManagementReportResponse> list() {
        return reportDao.findAll();
    }

    public ManagementReportResponse get(Long id) {
        return reportDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Management report " + id + " not found"));
    }

    static String narrate(RevenueSummary summary) {
        String categories = summary.byCategory().stream()
                .map(row -> row.category() + " " + row.amount().toPlainString())
                .collect(Collectors.joining(", "));
        if (categories.isEmpty()) {
            categories = "no revenue in this period";
        }
        return "Management report %s to %s: total %s %s. %s.".formatted(
                summary.from(),
                summary.to(),
                summary.totalAmount().toPlainString(),
                summary.currency(),
                categories);
    }

    private String writeJson(List<CategoryAmount> byCategory) {
        try {
            return objectMapper.writeValueAsString(byCategory);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not store the category breakdown", ex);
        }
    }
}
