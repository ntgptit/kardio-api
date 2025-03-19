package com.kardio.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic success response with a message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse {
	private String message;
	private Boolean success;

	public static SuccessResponse of(String message) {
		return SuccessResponse.builder().message(message).success(true).build();
	}
}