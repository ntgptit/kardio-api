package com.kardio.dto.vocabulary;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for importing multiple vocabulary items at once
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyBulkImportRequest {
	@NotNull(message = "Study module ID is required")
	private UUID moduleId;

	@NotNull(message = "Vocabulary items are required")
	private VocabularyCreateRequest[] items;
}