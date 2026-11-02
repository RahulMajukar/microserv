package com.learn.revenue.db;

import com.learn.revenue.api.RevenueResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RevenueRowMapperTest {

    @Test
    void mapsOneRevenuesRow() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(7L);
        when(rs.getString("source")).thenReturn("web-app");
        when(rs.getString("category")).thenReturn("ads");
        when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("300.00"));
        when(rs.getString("currency")).thenReturn("USD");
        when(rs.getObject("recorded_on", LocalDate.class)).thenReturn(LocalDate.parse("2026-01-20"));
        when(rs.getString("note")).thenReturn("January ads");

        RevenueResponse row = RevenueRowMapper.INSTANCE.mapRow(rs, 0);

        assertThat(row.id()).isEqualTo(7L);
        assertThat(row.category()).isEqualTo("ads");
        assertThat(row.amount()).isEqualByComparingTo("300.00");
        assertThat(row.recordedOn()).isEqualTo(LocalDate.parse("2026-01-20"));
    }
}
