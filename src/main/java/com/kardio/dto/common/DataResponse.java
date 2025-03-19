package com.kardio.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic data response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResponse<T> {
	private T data;
	private Boolean success;

	public static <T> DataResponse<T> of(T data) {
		return DataResponse.<T>builder().data(data).success(true).build();
	}
}
