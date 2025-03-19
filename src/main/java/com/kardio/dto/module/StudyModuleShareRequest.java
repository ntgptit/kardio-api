package com.kardio.dto.module;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sharing a module with users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyModuleShareRequest {
	@NotNull(message = "User IDs are required")
	private UUID[] userIds;
}