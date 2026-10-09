package com.janumart.exception;

import javax.servlet.http.HttpServletResponse;

/** 401 - not authenticated. */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}