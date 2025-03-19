package com.kardio.dto.progress;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kardio.entity.enums.LearningStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for learning progress response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressResponse {
	private UUID id;
	private UUID vocabularyId;
	private LearningStatus status;
	private Integer correctCount;
	private Integer incorrectCount;
	private Double accuracyRate;
	private LocalDateTime lastStudiedAt;
	private LocalDateTime nextReviewAt;
}
