package com.learn.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class JwtAuthFilterTest {

    private final JwtService jwtService = new JwtService("learning-lab-jwt-secret-change-me-32b", 60);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtService, new ObjectMapper());

    @Test
    void loginDoesNotNeedAToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void anApiCallWithoutATokenIs401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/revenues/summary");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Missing or invalid token");
        verifyNoInteractions(chain);
    }

    @Test
    void aValidTokenReachesTheController() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/revenues/summary");
        request.addHeader("Authorization", "Bearer " + jwtService.create("learner"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(request.getAttribute(JwtAuthFilter.USERNAME)).isEqualTo("learner");
        verify(chain).doFilter(request, response);
    }
}
