package com.learn.report.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.common.api.CategoryAmount;
import com.learn.report.api.ManagementReportResponse;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Turns one row from {@code management_reports} into the object the API returns.
 * The category breakdown is stored as JSON text and read back here.
 */
public class ManagementReportRowMapper implements RowMapper<ManagementReportResponse> {

    private static final TypeReference<List<CategoryAmount>> CATEGORY_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public ManagementReportRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ManagementReportResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            List<CategoryAmount> byCategory = objectMapper.readValue(rs.getString("breakdown_json"), CATEGORY_LIST);
            return new ManagementReportResponse(
                    rs.getLong("id"),
                    rs.getObject("period_from", LocalDate.class),
                    rs.getObject("period_to", LocalDate.class),
                    rs.getBigDecimal("total_revenue"),
                    rs.getString("currency"),
                    byCategory,
                    rs.getString("narrative"),
                    rs.getObject("created_at", LocalDateTime.class));
        } catch (JsonProcessingException ex) {
            throw new SQLException("Could not read breakdown_json", ex);
        }
    }
}
