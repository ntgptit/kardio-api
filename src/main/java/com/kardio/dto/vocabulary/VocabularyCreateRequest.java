package com.kardio.dto.vocabulary;

import java.util.UUID;

import com.kardio.entity.enums.DifficultyLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new vocabulary item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyCreateRequest {
	@NotNull(message = "Study module ID is required")
	private UUID moduleId;

	@NotBlank(message = "Term is required")
	private String term;

	@NotBlank(message = "Definition is required")
	private String definition;

	private String example;

	private String pronunciation;

	private String partOfSpeech;

	@Builder.Default
	private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;
}