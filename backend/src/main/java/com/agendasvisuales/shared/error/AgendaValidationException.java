package com.agendasvisuales.shared.error;

public class AgendaValidationException extends RuntimeException {

    public AgendaValidationException(String message) {
        super(message);
    }
}