package com.kardio.dto.classroom;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kardio.dto.module.StudyModuleResponse;
import com.kardio.dto.user.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for class module response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassModuleResponse {
	private UUID id;
	private UUID classId;
	private StudyModuleResponse module;
	private UserResponse addedBy;
	private LocalDateTime createdAt;
}
