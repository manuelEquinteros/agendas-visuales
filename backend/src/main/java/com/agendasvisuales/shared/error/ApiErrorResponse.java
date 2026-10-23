package com.agendasvisuales.shared.error;

public record ApiErrorResponse(
        ErrorCodeEnum code,
        String message,
        Object details
) {
}