import { AgendaRequest } from './agenda.models';

export type InterpretationStatus = 'COMPLETE' | 'INCOMPLETE' | 'OUT_OF_SCOPE';

export interface MissingField {
  field: string;

  question: string;
}

export interface InterpretAgendaRequest {
  message: string;

  previousUserMessage: string | null;

  previousAssistantMessage: string | null;

  context: AgendaRequest | null;
}

export interface InterpretationResult {
  status: InterpretationStatus;

  assistantMessage: string;

  missingFields: MissingField[];

  agenda: AgendaRequest | null;
}
