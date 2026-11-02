package com.learn.common.web;

import org.slf4j.MDC;

import java.util.UUID;

public final class RequestIds {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    private RequestIds() {
    }

    public static String current() {
        String id = MDC.get(MDC_KEY);
        if (id == null || id.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return id;
    }
}
