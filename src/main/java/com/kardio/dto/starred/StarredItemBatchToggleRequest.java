package com.kardio.dto.starred;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for batch starring items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarredItemBatchToggleRequest {
	@NotNull(message = "Vocabulary IDs are required")
	private UUID[] vocabularyIds;

	private Boolean starred;
}
