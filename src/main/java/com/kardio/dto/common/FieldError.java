package com.kardio.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Field error for validation responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldError {
	private String field;
	private String message;
}
