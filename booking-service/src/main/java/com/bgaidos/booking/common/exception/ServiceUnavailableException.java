package com.bgaidos.booking.common.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends BaseException {

    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
