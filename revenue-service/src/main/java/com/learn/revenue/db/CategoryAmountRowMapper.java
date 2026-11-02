package com.learn.revenue.db;

import com.learn.common.api.CategoryAmount;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryAmountRowMapper implements RowMapper<CategoryAmount> {

    public static final CategoryAmountRowMapper INSTANCE = new CategoryAmountRowMapper();

    @Override
    public CategoryAmount mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CategoryAmount(rs.getString("category"), rs.getBigDecimal("amount"));
    }
}
