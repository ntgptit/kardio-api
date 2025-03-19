package com.kardio.dto.session;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ending a study session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionEndRequest {
	private UUID sessionId;
}
