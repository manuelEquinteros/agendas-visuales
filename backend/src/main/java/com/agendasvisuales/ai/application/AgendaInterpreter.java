package com.agendasvisuales.ai.application;

import com.agendasvisuales.ai.model.InterpretAgendaRequest;
import com.agendasvisuales.ai.model.InterpretationResult;

public interface AgendaInterpreter {

    InterpretationResult interpret(
            InterpretAgendaRequest request
    );
}