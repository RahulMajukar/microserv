package com.learn.report.client;

import com.learn.common.api.ApiResponse;
import com.learn.common.api.RevenueSummary;
import com.learn.common.error.DownstreamException;
import com.learn.common.web.RequestIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;

/**
 * The only door from report-service into revenue-service.
 * This class sends HTTP. It does not query the revenues table.
 */
public class RevenueClient {

    private static final Logger log = LoggerFactory.getLogger(RevenueClient.class);
    private static final ParameterizedTypeReference<ApiResponse<RevenueSummary>> SUMMARY_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public RevenueClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public RevenueSummary fetchSummary(LocalDate from, LocalDate to) {
        log.info("HTTP GET revenue-service /api/revenues/summary from={} to={}", from, to);
        try {
            var call = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/revenues/summary")
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .build())
                    .header(RequestIds.HEADER, RequestIds.current());
            String authorization = currentAuthorization();
            if (authorization != null) {
                log.info("Forwarding caller token to revenue-service");
                call = call.header(HttpHeaders.AUTHORIZATION, authorization);
            }
            ApiResponse<RevenueSummary> body = call
                    .retrieve()
                    .onStatus(status -> status.isError(), (request, response) -> {
                        throw new DownstreamException(
                                "revenue-service responded with HTTP " + response.getStatusCode().value());
                    })
                    .body(SUMMARY_TYPE);
            if (body == null || body.responseBody() == null) {
                throw new DownstreamException("revenue-service returned an empty body");
            }
            return body.responseBody();
        } catch (DownstreamException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new DownstreamException("revenue-service is unreachable", ex);
        }
    }

    private static String currentAuthorization() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
