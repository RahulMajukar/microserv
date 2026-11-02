package com.learn.common.auth;

import com.learn.common.api.ApiResponse;
import com.learn.common.error.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Both services load this controller, so login works on port 8081 and port 8082.
 * The issued token is accepted by both because they share {@code auth.jwt.secret}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final String username;
    private final String password;

    public AuthController(
            JwtService jwtService,
            @Value("${auth.user.username}") String username,
            @Value("${auth.user.password}") String password) {
        this.jwtService = jwtService;
        this.username = username;
        this.password = password;
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new IllegalArgumentException("username and password are required");
        }
        if (!username.equals(request.username()) || !password.equals(request.password())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        TokenResponse token = new TokenResponse(jwtService.create(username), "Bearer", jwtService.ttlMinutes());
        return ApiResponse.ok(token);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
