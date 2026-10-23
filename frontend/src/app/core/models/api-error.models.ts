export type ApiErrorCode =
  | 'VALIDATION_ERROR'
  | 'AGENDA_NOT_FOUND'
  | 'AI_UNAVAILABLE'
  | 'AI_INVALID_RESPONSE'
  | 'ARASAAC_UNAVAILABLE'
  | 'PERSISTENCE_ERROR';

export interface ApiErrorResponse {
  code: ApiErrorCode;

  message: string;

  details: unknown;
}

export function getApiErrorCode(error: unknown): ApiErrorCode | null {
  const code = (
    error as {
      error?: {
        code?: unknown;
      };
    }
  )?.error?.code;

  switch (code) {
    case 'VALIDATION_ERROR':
    case 'AGENDA_NOT_FOUND':
    case 'AI_UNAVAILABLE':
    case 'AI_INVALID_RESPONSE':
    case 'ARASAAC_UNAVAILABLE':
    case 'PERSISTENCE_ERROR':
      return code;

    default:
      return null;
  }
}
