package com.kardio.dto.session;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for session results analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionItemAnalysisResponse {
	private Integer totalItems;
	private Integer correctItems;
	private Double accuracyRate;
	private Double averageResponseTimeMs;
	private List<String> difficultTerms;
	private List<String> masteredTerms;
}