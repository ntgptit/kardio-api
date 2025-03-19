package com.kardio.dto.progress;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for recording a study attempt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressAttemptRequest {
	private UUID vocabularyId;
	private Boolean isCorrect;
}