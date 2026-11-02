package com.learn.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.common.api.ApiResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Requires a Bearer token on /api calls, except login.
 * Each service checks the signature itself. It does not ask the other service.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String USERNAME = "username";

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!requiresToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            reject(response);
            return;
        }
        try {
            String username = jwtService.subject(header.substring("Bearer ".length()).trim());
            request.setAttribute(USERNAME, username);
            log.info("Authenticated user {}", username);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            log.info("Rejected token for {} {}", request.getMethod(), path(request));
            reject(response);
        }
    }

    private static boolean requiresToken(HttpServletRequest request) {
        String path = path(request);
        if (!path.startsWith("/api/")) {
            return false;
        }
        return !(path.equals("/api/auth/login") && "POST".equalsIgnoreCase(request.getMethod()));
    }

    private static String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            return uri.substring(context.length());
        }
        return uri;
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail("Missing or invalid token", 401));
    }
}
