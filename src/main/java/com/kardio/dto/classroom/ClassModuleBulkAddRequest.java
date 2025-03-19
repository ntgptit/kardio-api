package com.kardio.dto.classroom;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bulk adding modules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassModuleBulkAddRequest {
	@NotNull(message = "Module IDs are required")
	private UUID[] moduleIds;
}
