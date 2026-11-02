package com.learn.common.web;

import java.time.LocalDate;

public final class DateRanges {

    private DateRanges() {
    }

    public static void requireFromNotAfterTo(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
    }
}
