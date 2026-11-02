package com.learn.common.auth;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "learning-lab-jwt-secret-change-me-32b";

    @Test
    void tokenCarriesTheUsername() {
        JwtService jwtService = new JwtService(SECRET, 60);

        String token = jwtService.create("learner");

        assertThat(jwtService.subject(token)).isEqualTo("learner");
    }

    @Test
    void aChangedTokenIsRejected() {
        JwtService jwtService = new JwtService(SECRET, 60);
        String token = jwtService.create("learner");

        assertThatThrownBy(() -> jwtService.subject(token + "x"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void aTokenFromAnotherSecretIsRejected() {
        String token = new JwtService(SECRET, 60).create("learner");
        JwtService other = new JwtService("a-different-learning-lab-secret-32b!", 60);

        assertThatThrownBy(() -> other.subject(token))
                .isInstanceOf(JwtException.class);
    }
}
