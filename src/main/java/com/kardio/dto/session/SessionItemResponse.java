package com.kardio.dto.session;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for session item response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionItemResponse {
    private UUID id;
    private UUID vocabularyId;
    private String term;
    private String definition;
    private Boolean isCorrect;
    private Integer responseTimeMs;
    private Double responseTimeSeconds;
    private LocalDateTime createdAt;
}