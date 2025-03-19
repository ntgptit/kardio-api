package com.kardio.dto.streak;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for streak response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {
	private UUID id;
	private Integer currentStreak;
	private Integer longestStreak;
	private LocalDate lastActivityDate;
	private Boolean isActiveToday;
}