package com.kardio.dto.classroom;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adding a module to a class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassModuleAddRequest {
	@NotNull(message = "Module ID is required")
	private UUID moduleId;
}