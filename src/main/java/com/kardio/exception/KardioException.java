package com.kardio.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Base exception for all business logic related exceptions in the Kardio
 * application. Extends RuntimeException to avoid forcing callers to catch or
 * declare exceptions.
 */
@Getter
public class KardioException extends RuntimeException {

	private static final long serialVersionUID = 1509401079030096266L;
	private final HttpStatus status;

	/**
	 * Creates a new Kardio exception with the given message and HTTP status.
	 *
	 * @param message Error message
	 * @param status  HTTP status to return
	 */
	public KardioException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	/**
	 * Creates a new Kardio exception with the given message, cause, and HTTP
	 * status.
	 *
	 * @param message Error message
	 * @param cause   The cause of the exception
	 * @param status  HTTP status to return
	 */
	public KardioException(String message, Throwable cause, HttpStatus status) {
		super(message, cause);
		this.status = status;
	}

	/**
	 * Factory method for creating a resource not found exception.
	 *
	 * @param resourceName Name of the resource
	 * @param id           Identifier of the resource
	 * @return A new KardioException with NOT_FOUND status
	 */
	public static KardioException resourceNotFound(String resourceName, Object id) {
		return new KardioException(String.format("%s not found with id: %s", resourceName, id), HttpStatus.NOT_FOUND);
	}

	/**
	 * Factory method for creating a resource already exists exception.
	 *
	 * @param resourceName Name of the resource
	 * @param field        Field name that must be unique
	 * @param value        Value that already exists
	 * @return A new KardioException with CONFLICT status
	 */
	public static KardioException resourceAlreadyExists(String resourceName, String field, Object value) {
		return new KardioException(String.format("%s already exists with %s: %s", resourceName, field, value),
				HttpStatus.CONFLICT);
	}

	/**
	 * Factory method for creating a validation exception.
	 *
	 * @param message Validation error message
	 * @return A new KardioException with BAD_REQUEST status
	 */
	public static KardioException validationError(String message) {
		return new KardioException(message, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Factory method for creating an unauthorized operation exception.
	 *
	 * @param message Error message
	 * @return A new KardioException with FORBIDDEN status
	 */
	public static KardioException forbidden(String message) {
		return new KardioException(message, HttpStatus.FORBIDDEN);
	}
}