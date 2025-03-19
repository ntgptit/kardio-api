package com.kardio.dto.progress;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stats summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
	private Integer totalVocabularies;
	private Integer masteredCount;
	private Integer learningCount;
	private Integer notStudiedCount;
	private Double overallAccuracy;
	private Integer currentStreak;
	private Integer longestStreak;
	private LocalDateTime lastStudyDate;
}