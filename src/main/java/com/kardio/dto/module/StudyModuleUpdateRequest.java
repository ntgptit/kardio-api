package com.kardio.dto.module;

import java.util.UUID;

import com.kardio.entity.enums.VisibilityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a study module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyModuleUpdateRequest {
	private String name;
	private String description;
	private UUID folderId;
	private VisibilityType visibility;
}
