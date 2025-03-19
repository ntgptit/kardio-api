package com.kardio.dto.progress;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating review schedule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressUpdateScheduleRequest {
	private UUID vocabularyId;
	private LocalDateTime nextReviewAt;
}
