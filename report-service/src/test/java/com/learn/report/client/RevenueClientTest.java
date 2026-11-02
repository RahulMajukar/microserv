package com.learn.report.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learn.common.api.RevenueSummary;
import com.learn.common.error.DownstreamException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RevenueClientTest {

    @Test
    void readsTheSharedSummaryContract() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        RestClient.Builder builder = RestClient.builder().messageConverters(converters -> {
            converters.clear();
            converters.add(new MappingJackson2HttpMessageConverter(mapper));
        });
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(containsString("/api/revenues/summary")))
                .andRespond(withSuccess("""
                        {
                          "responseCode": 200,
                          "responseMessage": "SUCCESS",
                          "responseBody": {
                            "from": "2026-01-01",
                            "to": "2026-01-31",
                            "totalAmount": 1500.00,
                            "currency": "USD",
                            "byCategory": [
                              {"category": "ads", "amount": 300.00}
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        RevenueClient client = new RevenueClient(builder.baseUrl("http://revenue.test").build());
        RevenueSummary summary = client.fetchSummary(
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-31"));

        assertThat(summary.totalAmount()).isEqualByComparingTo("1500.00");
        assertThat(summary.currency()).isEqualTo("USD");
        assertThat(summary.byCategory()).hasSize(1);
        server.verify();
    }

    @Test
    void forwardsTheCallerToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(containsString("/api/revenues/summary")))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer caller-token"))
                .andRespond(withSuccess("""
                        {
                          "responseCode": 200,
                          "responseMessage": "SUCCESS",
                          "responseBody": {
                            "from": "2026-01-01",
                            "to": "2026-01-31",
                            "totalAmount": 1.00,
                            "currency": "USD",
                            "byCategory": []
                          }
                        }
                        """, MediaType.APPLICATION_JSON));
        MockHttpServletRequest incoming = new MockHttpServletRequest();
        incoming.addHeader(HttpHeaders.AUTHORIZATION, "Bearer caller-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(incoming));
        try {
            RevenueClient client = new RevenueClient(builder.baseUrl("http://revenue.test").build());
            client.fetchSummary(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"));
            server.verify();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void turnsAnErrorStatusIntoADownstreamException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(containsString("/api/revenues/summary")))
                .andRespond(withServerError());

        RevenueClient client = new RevenueClient(builder.baseUrl("http://revenue.test").build());

        assertThatThrownBy(() -> client.fetchSummary(
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-31")))
                .isInstanceOf(DownstreamException.class)
                .hasMessageContaining("500");
    }
}
