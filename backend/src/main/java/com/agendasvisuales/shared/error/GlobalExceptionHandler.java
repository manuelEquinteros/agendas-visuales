package com.agendasvisuales.shared.error;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AgendaValidationException.class)
	public ResponseEntity<ApiErrorResponse> handleAgendaValidation(AgendaValidationException exception) {

		ApiErrorResponse error = new ApiErrorResponse(ErrorCodeEnum.VALIDATION_ERROR, exception.getMessage(), null);

		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {

		List<String> details = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).distinct().toList();

		ApiErrorResponse error = new ApiErrorResponse(ErrorCodeEnum.VALIDATION_ERROR,
				"La solicitud contiene datos inválidos.", details);

		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException exception) {

		ApiErrorResponse error = new ApiErrorResponse(ErrorCodeEnum.VALIDATION_ERROR,
				"El contenido de la solicitud no es válido.", null);

		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(AgendaNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleAgendaNotFound(AgendaNotFoundException exception) {

		ApiErrorResponse error = new ApiErrorResponse(ErrorCodeEnum.AGENDA_NOT_FOUND, exception.getMessage(), null);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiErrorResponse> handleDataAccess(DataAccessException exception) {

		ApiErrorResponse error = new ApiErrorResponse(ErrorCodeEnum.PERSISTENCE_ERROR,
				"No fue posible acceder a los datos en este momento.", null);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

	@ExceptionHandler(AiUnavailableException.class)
	public ResponseEntity<ApiErrorResponse> handleAiUnavailable(AiUnavailableException exception) {

		ApiErrorResponse error = new ApiErrorResponse(ErrorCodeEnum.AI_UNAVAILABLE,
				"El servicio de inteligencia artificial no está disponible en este momento.", null);

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
	}

	@ExceptionHandler(ArasaacUnavailableException.class)
	public ResponseEntity<ApiErrorResponse> handleArasaacUnavailable(ArasaacUnavailableException exception) {

		ApiErrorResponse error = new ApiErrorResponse(ErrorCodeEnum.ARASAAC_UNAVAILABLE,
				"El servicio de pictogramas ARASAAC no está disponible en este momento.", null);

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
	}

	@ExceptionHandler(AiInvalidResponseException.class)
	public ResponseEntity<ApiErrorResponse> handleAiInvalidResponse(AiInvalidResponseException exception) {

		ApiErrorResponse error = new ApiErrorResponse(ErrorCodeEnum.AI_INVALID_RESPONSE,
				"La respuesta de la IA no pudo validarse.", exception.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
	}
}