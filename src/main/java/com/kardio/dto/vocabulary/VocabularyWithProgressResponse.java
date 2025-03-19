package com.kardio.dto.vocabulary;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kardio.dto.progress.LearningProgressResponse;
import com.kardio.entity.enums.DifficultyLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for vocabulary with learning progress
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyWithProgressResponse {
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
	private LearningProgressResponse progress;
}