package com.learn.common.auth;

public record TokenResponse(String token, String tokenType, int expiresInMinutes) {
}
