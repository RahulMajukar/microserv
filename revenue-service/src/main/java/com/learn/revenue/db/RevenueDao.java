package com.learn.revenue.db;

import com.learn.common.api.CategoryAmount;
import com.learn.revenue.api.RevenueResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class RevenueDao {

    private static final String COLUMNS = "id, source, category, amount, currency, recorded_on, note";

    private static final String INSERT = """
            INSERT INTO revenues (source, category, amount, currency, recorded_on, note)
            VALUES (:source, :category, :amount, :currency, :recordedOn, :note)
            """;

    private static final String FIND_BY_ID = """
            SELECT %s
            FROM revenues
            WHERE id = :id
            """.formatted(COLUMNS);

    private static final String FIND_ALL = """
            SELECT %s
            FROM revenues
            ORDER BY recorded_on DESC, id DESC
            """.formatted(COLUMNS);

    private static final String FIND_BETWEEN = """
            SELECT %s
            FROM revenues
            WHERE recorded_on BETWEEN :from AND :to
            ORDER BY recorded_on ASC, id ASC
            """.formatted(COLUMNS);

    private static final String SUM_BY_CATEGORY = """
            SELECT category, SUM(amount) AS amount
            FROM revenues
            WHERE recorded_on BETWEEN :from AND :to
            GROUP BY category
            ORDER BY category
            """;

    private static final String FIND_CURRENCIES = """
            SELECT DISTINCT currency
            FROM revenues
            WHERE recorded_on BETWEEN :from AND :to
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public RevenueDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(String source, String category, BigDecimal amount, String currency, LocalDate recordedOn, String note) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("source", source)
                .addValue("category", category)
                .addValue("amount", amount)
                .addValue("currency", currency)
                .addValue("recordedOn", recordedOn)
                .addValue("note", note);
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(INSERT, params, keys, new String[]{"id"});
        Number id = keys.getKey();
        if (id == null) {
            throw new IllegalStateException("Insert into revenues did not return an id");
        }
        return id.longValue();
    }

    public Optional<RevenueResponse> findById(long id) {
        List<RevenueResponse> rows = jdbc.query(
                FIND_BY_ID,
                new MapSqlParameterSource("id", id),
                RevenueRowMapper.INSTANCE);
        return rows.stream().findFirst();
    }

    public List<RevenueResponse> findAll() {
        return jdbc.query(FIND_ALL, RevenueRowMapper.INSTANCE);
    }

    public List<RevenueResponse> findBetween(LocalDate from, LocalDate to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to);
        return jdbc.query(FIND_BETWEEN, params, RevenueRowMapper.INSTANCE);
    }

    public List<CategoryAmount> sumByCategory(LocalDate from, LocalDate to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to);
        return jdbc.query(SUM_BY_CATEGORY, params, CategoryAmountRowMapper.INSTANCE);
    }

    public List<String> findCurrencies(LocalDate from, LocalDate to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to);
        return jdbc.query(FIND_CURRENCIES, params, (rs, rowNum) -> rs.getString("currency"));
    }
}
