package com.kardio.dto.session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.kardio.dto.module.StudyModuleResponse;
import com.kardio.entity.enums.SessionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for detailed study session with items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionDetailedResponse {
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
	private List<SessionItemResponse> items;
	private StudyModuleResponse module;
}