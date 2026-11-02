package com.learn.report.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.report.api.ManagementReportResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ManagementReportDao {

    private static final String COLUMNS =
            "id, period_from, period_to, total_revenue, currency, breakdown_json, narrative, created_at";

    private static final String INSERT = """
            INSERT INTO management_reports
                (period_from, period_to, total_revenue, currency, breakdown_json, narrative, created_at)
            VALUES
                (:periodFrom, :periodTo, :totalRevenue, :currency, :breakdownJson, :narrative, :createdAt)
            """;

    private static final String FIND_BY_ID = """
            SELECT %s
            FROM management_reports
            WHERE id = :id
            """.formatted(COLUMNS);

    private static final String FIND_ALL = """
            SELECT %s
            FROM management_reports
            ORDER BY created_at DESC, id DESC
            """.formatted(COLUMNS);

    private final NamedParameterJdbcTemplate jdbc;
    private final ManagementReportRowMapper rowMapper;

    public ManagementReportDao(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.rowMapper = new ManagementReportRowMapper(objectMapper);
    }

    public long insert(
            LocalDate periodFrom,
            LocalDate periodTo,
            BigDecimal totalRevenue,
            String currency,
            String breakdownJson,
            String narrative,
            LocalDateTime createdAt) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("periodFrom", periodFrom)
                .addValue("periodTo", periodTo)
                .addValue("totalRevenue", totalRevenue)
                .addValue("currency", currency)
                .addValue("breakdownJson", breakdownJson)
                .addValue("narrative", narrative)
                .addValue("createdAt", createdAt);
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(INSERT, params, keys, new String[]{"id"});
        Number id = keys.getKey();
        if (id == null) {
            throw new IllegalStateException("Insert into management_reports did not return an id");
        }
        return id.longValue();
    }

    public Optional<ManagementReportResponse> findById(long id) {
        List<ManagementReportResponse> rows = jdbc.query(
                FIND_BY_ID,
                new MapSqlParameterSource("id", id),
                rowMapper);
        return rows.stream().findFirst();
    }

    public List<ManagementReportResponse> findAll() {
        return jdbc.query(FIND_ALL, rowMapper);
    }
}
