package com.kardio.dto.session;

import java.util.UUID;

import com.kardio.entity.StudySession.SessionType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new study session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionCreateRequest {
	@NotNull(message = "Module ID is required")
	private UUID moduleId;

	@NotNull(message = "Session type is required")
	private SessionType sessionType;
}