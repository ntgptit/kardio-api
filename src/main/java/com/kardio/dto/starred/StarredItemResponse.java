package com.kardio.dto.starred;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kardio.dto.vocabulary.VocabularyResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for starred item response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarredItemResponse {
	private UUID id;
	private VocabularyResponse vocabulary;
	private LocalDateTime createdAt;
}