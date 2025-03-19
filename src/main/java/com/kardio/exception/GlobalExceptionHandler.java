package com.kardio.exception;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the application. Centralizes exception handling
 * for all controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * Handle custom Kardio exceptions
	 */
	@ExceptionHandler(KardioException.class)
	public ResponseEntity<ApiError> handleKardioException(KardioException ex, WebRequest request) {
		log.error("Business exception: {}", ex.getMessage());

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now()).status(ex.getStatus().value())
				.error(ex.getStatus().getReasonPhrase()).message(ex.getMessage())
				.path(request.getDescription(false).replace("uri=", "")).build();

		return new ResponseEntity<>(apiError, ex.getStatus());
	}

	/**
	 * Handle entity not found exceptions
	 */
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiError> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
		log.error("Entity not found: {}", ex.getMessage());

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now()).status(HttpStatus.NOT_FOUND.value())
				.error(HttpStatus.NOT_FOUND.getReasonPhrase()).message(ex.getMessage())
				.path(request.getDescription(false).replace("uri=", "")).build();

		return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
	}

	/**
	 * Handle validation exceptions from @Valid
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		log.error("Validation error: {}", ex.getMessage());

		// Group field errors by field name
		final Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(FieldError::getField,
						fieldError -> fieldError.getDefaultMessage() == null ? "Invalid value"
								: fieldError.getDefaultMessage(),
						// If multiple errors for same field, join them
						(error1, error2) -> error1 + "; " + error2));

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message("Validation failed").path(request.getDescription(false).replace("uri=", "")).errors(errors)
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle constraint violation exceptions (validation errors)
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException ex,
			WebRequest request) {

		log.error("Constraint violation: {}", ex.getMessage());

		final Map<String, String> errors = ex.getConstraintViolations().stream()
				.collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(),
						ConstraintViolation::getMessage, (error1, error2) -> error1 + "; " + error2));

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message("Validation failed").path(request.getDescription(false).replace("uri=", "")).errors(errors)
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle data integrity violations (e.g., unique constraint violations)
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
			WebRequest request) {

		log.error("Data integrity violation: {}", ex.getMessage());

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now()).status(HttpStatus.CONFLICT.value())
				.error(HttpStatus.CONFLICT.getReasonPhrase()).message("Database constraint violation")
				.path(request.getDescription(false).replace("uri=", "")).build();

		return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
	}

	/**
	 * Handle authentication exceptions
	 */
	@ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
	public ResponseEntity<ApiError> handleAuthenticationException(Exception ex, WebRequest request) {

		log.error("Authentication error: {}", ex.getMessage());

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.UNAUTHORIZED.value()).error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
				.message("Authentication failed: " + ex.getMessage())
				.path(request.getDescription(false).replace("uri=", "")).build();

		return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Handle access denied exceptions
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {

		log.error("Access denied: {}", ex.getMessage());

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now()).status(HttpStatus.FORBIDDEN.value())
				.error(HttpStatus.FORBIDDEN.getReasonPhrase()).message("Access denied: insufficient permissions")
				.path(request.getDescription(false).replace("uri=", "")).build();

		return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
	}

	/**
	 * Handle missing request parameters
	 */
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		log.error("Missing parameter: {}", ex.getMessage());

		final Map<String, String> errors = new HashMap<>();
		errors.put(ex.getParameterName(), "Parameter is missing");

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message("Required request parameter is missing")
				.path(request.getDescription(false).replace("uri=", "")).errors(errors).build();

		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle method argument type mismatch
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
			WebRequest request) {

		log.error("Type mismatch: {}", ex.getMessage());

		final Map<String, String> errors = new HashMap<>();
		errors.put(ex.getName(), "Should be of type "
				+ (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"));

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message("Type mismatch for parameter").path(request.getDescription(false).replace("uri=", ""))
				.errors(errors).build();

		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Fallback for all other exceptions
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleAll(Exception ex, WebRequest request) {
		log.error("Unhandled exception", ex);

		final ApiError apiError = ApiError.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()).message("An unexpected error occurred")
				.path(request.getDescription(false).replace("uri=", "")).build();

		return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}