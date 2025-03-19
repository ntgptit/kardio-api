package com.kardio.dto.module;

import java.util.UUID;

import com.kardio.entity.enums.VisibilityType;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new study module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyModuleCreateRequest {
	@NotBlank(message = "Module name is required")
	private String name;

	private String description;

	private UUID folderId;

	@Builder.Default
	private VisibilityType visibility = VisibilityType.PRIVATE;
}
