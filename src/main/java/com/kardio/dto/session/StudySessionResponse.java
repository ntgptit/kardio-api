package com.kardio.dto.session;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kardio.entity.enums.SessionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for study session response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionResponse {
    private UUID id;
    private UUID moduleId;
    private String moduleName;
    private SessionType sessionType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalItems;
    private Integer correctItems;
    private Double accuracyRate;
    private Long durationSeconds;
}