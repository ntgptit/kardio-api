package com.kardio.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for batch updating learning progress
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressBatchUpdateRequest {
	private LearningProgressAttemptRequest[] attempts;
}
