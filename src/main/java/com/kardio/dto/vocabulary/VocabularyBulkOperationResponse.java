package com.kardio.dto.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bulk operations response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyBulkOperationResponse {
	private int successCount;
	private int failCount;
	private String[] errors;
}