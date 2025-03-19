package com.kardio.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassCreateRequest {
	@NotBlank(message = "Class name is required")
	private String name;

	private String description;
}