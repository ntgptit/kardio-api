package com.kardio.dto.vocabulary;

import com.kardio.entity.enums.DifficultyLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a vocabulary item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyUpdateRequest {
	private String term;
	private String definition;
	private String example;
	private String pronunciation;
	private String partOfSpeech;
	private DifficultyLevel difficultyLevel;
}
