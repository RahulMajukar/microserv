package com.learn.common.web;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangesTest {

    @Test
    void allowsSameDay() {
        LocalDate day = LocalDate.parse("2026-01-15");
        assertThatCode(() -> DateRanges.requireFromNotAfterTo(day, day)).doesNotThrowAnyException();
    }

    @Test
    void rejectsReversedRange() {
        assertThatThrownBy(() -> DateRanges.requireFromNotAfterTo(
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-01-01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("from must be on or before to");
    }
}
