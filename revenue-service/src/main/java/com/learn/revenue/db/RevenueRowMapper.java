package com.learn.revenue.db;

import com.learn.revenue.api.RevenueResponse;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Turns one row from {@code revenues} into the object the API returns.
 */
public class RevenueRowMapper implements RowMapper<RevenueResponse> {

    public static final RevenueRowMapper INSTANCE = new RevenueRowMapper();

    @Override
    public RevenueResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RevenueResponse(
                rs.getLong("id"),
                rs.getString("source"),
                rs.getString("category"),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                rs.getObject("recorded_on", LocalDate.class),
                rs.getString("note"));
    }
}
