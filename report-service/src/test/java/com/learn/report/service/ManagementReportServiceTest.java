package com.learn.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.common.api.CategoryAmount;
import com.learn.common.api.RevenueSummary;
import com.learn.common.error.DownstreamException;
import com.learn.report.api.ManagementReportResponse;
import com.learn.report.client.RevenueClient;
import com.learn.report.db.ManagementReportDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagementReportServiceTest {

    private static final LocalDate FROM = LocalDate.parse("2026-01-01");
    private static final LocalDate TO = LocalDate.parse("2026-01-31");

    @Mock
    private ManagementReportDao reportDao;

    @Mock
    private RevenueClient revenueClient;

    private ManagementReportService service;

    @BeforeEach
    void setUp() {
        service = new ManagementReportService(reportDao, revenueClient, new ObjectMapper());
    }

    @Test
    void storesASnapshotOfTheRevenueSummary() {
        when(revenueClient.fetchSummary(FROM, TO)).thenReturn(new RevenueSummary(
                FROM,
                TO,
                new BigDecimal("1500.00"),
                "USD",
                List.of(
                        new CategoryAmount("ads", new BigDecimal("300.00")),
                        new CategoryAmount("subscriptions", new BigDecimal("1200.00")))));
        when(reportDao.insert(eq(FROM), eq(TO), any(), eq("USD"), any(), any(), any())).thenReturn(42L);
        when(reportDao.findById(42L)).thenReturn(Optional.of(new ManagementReportResponse(
                42L,
                FROM,
                TO,
                new BigDecimal("1500.00"),
                "USD",
                List.of(
                        new CategoryAmount("ads", new BigDecimal("300.00")),
                        new CategoryAmount("subscriptions", new BigDecimal("1200.00"))),
                "stored",
                LocalDateTime.parse("2026-03-01T00:00:00"))));

        ManagementReportResponse response = service.generate(FROM, TO);

        ArgumentCaptor<String> narrative = ArgumentCaptor.forClass(String.class);
        verify(reportDao).insert(eq(FROM), eq(TO), any(), eq("USD"), any(), narrative.capture(), any());
        assertThat(response.id()).isEqualTo(42L);
        assertThat(narrative.getValue()).contains("total 1500.00 USD");
        assertThat(narrative.getValue()).contains("subscriptions 1200.00");
    }

    @Test
    void doesNotStoreAReportWhenRevenueServiceFails() {
        when(revenueClient.fetchSummary(FROM, TO))
                .thenThrow(new DownstreamException("revenue-service is unreachable"));

        assertThatThrownBy(() -> service.generate(FROM, TO)).isInstanceOf(DownstreamException.class);
        verify(reportDao, never()).insert(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void rejectsAReversedRangeBeforeCallingRevenue() {
        assertThatThrownBy(() -> service.generate(TO, FROM)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(revenueClient);
        verifyNoInteractions(reportDao);
    }
}
