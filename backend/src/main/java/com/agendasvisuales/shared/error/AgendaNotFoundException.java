package com.agendasvisuales.shared.error;

public class AgendaNotFoundException extends RuntimeException {

    public AgendaNotFoundException(Long id) {
        super("Agenda no encontrada: " + id);
    }
}