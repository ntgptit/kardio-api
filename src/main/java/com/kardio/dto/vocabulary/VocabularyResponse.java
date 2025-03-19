package com.kardio.dto.vocabulary;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kardio.entity.enums.DifficultyLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for vocabulary response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyResponse {
	private UUID id;
	private UUID moduleId;
	private String term;
	private String definition;
	private String example;
	private String pronunciation;
	private String partOfSpeech;
	private DifficultyLevel difficultyLevel;
	private LocalDateTime createdAt;
	private Boolean isStarred;
}