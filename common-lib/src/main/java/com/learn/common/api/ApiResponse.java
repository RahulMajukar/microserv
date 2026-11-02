package com.learn.common.api;

/**
 * Every HTTP response from both services uses this envelope.
 * {@code responseCode} matches the HTTP status.
 */
public record ApiResponse<T>(int responseCode, String responseMessage, T responseBody) {

    public static <T> ApiResponse<T> ok(T body) {
        return new ApiResponse<>(200, "SUCCESS", body);
    }

    public static <T> ApiResponse<T> created(T body) {
        return new ApiResponse<>(201, "SUCCESS", body);
    }

    public static <T> ApiResponse<T> fail(String message, int code) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> fail(String message, T body, int code) {
        return new ApiResponse<>(code, message, body);
    }
}
