package com.learn.revenue.service;

import com.learn.common.api.CategoryAmount;
import com.learn.common.api.RevenueSummary;
import com.learn.revenue.db.RevenueDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    private static final LocalDate FROM = LocalDate.parse("2026-01-01");
    private static final LocalDate TO = LocalDate.parse("2026-01-31");

    @Mock
    private RevenueDao revenueDao;

    @InjectMocks
    private RevenueService service;

    @Test
    void totalsCategoriesInOneCurrency() {
        when(revenueDao.sumByCategory(FROM, TO)).thenReturn(List.of(
                new CategoryAmount("ads", new BigDecimal("300.00")),
                new CategoryAmount("subscriptions", new BigDecimal("1200.00"))));
        when(revenueDao.findCurrencies(FROM, TO)).thenReturn(List.of("USD"));

        RevenueSummary summary = service.summarize(FROM, TO);

        assertThat(summary.totalAmount()).isEqualByComparingTo("1500.00");
        assertThat(summary.currency()).isEqualTo("USD");
        assertThat(summary.byCategory()).hasSize(2);
    }

    @Test
    void emptyPeriodIsZeroUsd() {
        when(revenueDao.sumByCategory(FROM, TO)).thenReturn(List.of());
        when(revenueDao.findCurrencies(FROM, TO)).thenReturn(List.of());

        RevenueSummary summary = service.summarize(FROM, TO);

        assertThat(summary.totalAmount()).isEqualByComparingTo("0.00");
        assertThat(summary.currency()).isEqualTo("USD");
        assertThat(summary.byCategory()).isEmpty();
    }

    @Test
    void marksMixedCurrencies() {
        when(revenueDao.sumByCategory(FROM, TO)).thenReturn(List.of(
                new CategoryAmount("ads", new BigDecimal("10.00"))));
        when(revenueDao.findCurrencies(FROM, TO)).thenReturn(List.of("EUR", "USD"));

        RevenueSummary summary = service.summarize(FROM, TO);

        assertThat(summary.currency()).isEqualTo("MIXED");
    }

    @Test
    void rejectsReversedRangeBeforeQuerying() {
        assertThatThrownBy(() -> service.summarize(TO, FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("from must be on or before to");
        verifyNoInteractions(revenueDao);
    }
}
