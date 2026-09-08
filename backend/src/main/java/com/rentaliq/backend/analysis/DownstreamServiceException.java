package com.rentaliq.backend.analysis;

public class DownstreamServiceException extends RuntimeException {
    public DownstreamServiceException(String message) {
        super(message);
    }

    public DownstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
