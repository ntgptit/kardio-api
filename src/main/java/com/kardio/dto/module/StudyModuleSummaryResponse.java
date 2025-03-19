package com.kardio.dto.module;

import java.util.UUID;

import com.kardio.dto.user.UserResponse;
import com.kardio.entity.enums.VisibilityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for module summary (used in lists)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyModuleSummaryResponse {
	private UUID id;
	private String name;
	private VisibilityType visibility;
	private Integer vocabularyCount;
	private UserResponse creator;
}
