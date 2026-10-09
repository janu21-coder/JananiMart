package com.janumart.exception;

import javax.servlet.http.HttpServletResponse;

/** 403 - authenticated but not permitted. */
public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super(HttpServletResponse.SC_FORBIDDEN, message);
    }
}