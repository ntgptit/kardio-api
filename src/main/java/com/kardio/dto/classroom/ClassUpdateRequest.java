package com.kardio.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassUpdateRequest {
	private String name;
	private String description;
}
