package com.janumart.exception;

import javax.servlet.http.HttpServletResponse;

/** 400 - invalid input. */
public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(HttpServletResponse.SC_BAD_REQUEST, message);
    }
}