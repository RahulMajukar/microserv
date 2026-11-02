package com.learn.revenue.service;

import com.learn.common.api.CategoryAmount;
import com.learn.common.api.RevenueSummary;
import com.learn.common.error.ResourceNotFoundException;
import com.learn.common.web.DateRanges;
import com.learn.revenue.api.CreateRevenueRequest;
import com.learn.revenue.api.RevenueResponse;
import com.learn.revenue.db.RevenueDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class RevenueService {

    private static final Logger log = LoggerFactory.getLogger(RevenueService.class);

    private final RevenueDao revenueDao;

    public RevenueService(RevenueDao revenueDao) {
        this.revenueDao = revenueDao;
    }

    public RevenueResponse create(CreateRevenueRequest request) {
        long id = revenueDao.insert(
                request.source().trim(),
                request.category().trim(),
                money(request.amount()),
                request.currency().trim().toUpperCase(Locale.ROOT),
                request.recordedOn(),
                blankToNull(request.note()));
        RevenueResponse saved = get(id);
        log.info("Stored revenue id={} category={} amount={} {}",
                saved.id(), saved.category(), saved.amount(), saved.currency());
        return saved;
    }

    public RevenueResponse get(Long id) {
        return revenueDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revenue " + id + " not found"));
    }

    public List<RevenueResponse> list(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return revenueDao.findAll();
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to must be provided together");
        }
        DateRanges.requireFromNotAfterTo(from, to);
        return revenueDao.findBetween(from, to);
    }

    public RevenueSummary summarize(LocalDate from, LocalDate to) {
        DateRanges.requireFromNotAfterTo(from, to);
        log.info("Summarizing revenue from {} to {}", from, to);
        List<CategoryAmount> byCategory = revenueDao.sumByCategory(from, to).stream()
                .map(row -> new CategoryAmount(row.category(), money(row.amount())))
                .toList();
        BigDecimal total = byCategory.stream()
                .map(CategoryAmount::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String currency = currencyOf(revenueDao.findCurrencies(from, to));
        return new RevenueSummary(from, to, money(total), currency, byCategory);
    }

    private static String currencyOf(List<String> currencies) {
        return switch (currencies.size()) {
            case 0 -> "USD";
            case 1 -> currencies.get(0);
            default -> "MIXED";
        };
    }

    private static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
