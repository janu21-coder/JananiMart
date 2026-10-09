package com.janumart.exception;

import javax.servlet.http.HttpServletResponse;

/** 404 - resource not found. */
public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(HttpServletResponse.SC_NOT_FOUND, message);
    }
}