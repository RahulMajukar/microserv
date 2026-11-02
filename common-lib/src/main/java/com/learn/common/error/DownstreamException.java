package com.learn.common.error;

import org.springframework.web.client.RestClientException;

/**
 * Thrown when this service called another service and that call failed.
 * Extends RestClientException so Spring's HTTP client rethrows it instead of wrapping it.
 */
public class DownstreamException extends RestClientException {

    public DownstreamException(String message) {
        super(message);
    }

    public DownstreamException(String message, Throwable cause) {
        super(message, cause);
    }
}
