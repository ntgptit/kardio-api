package com.kardio.dto.session;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for recording a session item attempt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionRecordAttemptRequest {
    @NotNull(message = "Session ID is required")
    private UUID sessionId;

    @NotNull(message = "Vocabulary ID is required")
    private UUID vocabularyId;

    private Boolean isCorrect;

    @Min(value = 0, message = "Response time cannot be negative")
    private Integer responseTimeMs;
}