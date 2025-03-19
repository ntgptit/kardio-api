package com.kardio.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error response with message and error details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
	private String message;
	private String error;
	private Integer status;

	public static ErrorResponse of(String message, String error, Integer status) {
		return ErrorResponse.builder().message(message).error(error).status(status).build();
	}
}