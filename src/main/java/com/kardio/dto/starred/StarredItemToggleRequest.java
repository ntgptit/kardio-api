package com.kardio.dto.starred;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for starring/unstarring an item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarredItemToggleRequest {
	@NotNull(message = "Vocabulary ID is required")
	private UUID vocabularyId;

	private Boolean starred;
}
