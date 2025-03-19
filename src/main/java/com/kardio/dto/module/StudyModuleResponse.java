package com.kardio.dto.module;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kardio.dto.folder.FolderResponse;
import com.kardio.dto.user.UserResponse;
import com.kardio.entity.enums.VisibilityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for study module response (basic info)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyModuleResponse {
	private UUID id;
	private String name;
	private String description;
	private VisibilityType visibility;
	private LocalDateTime lastStudiedAt;
	private LocalDateTime createdAt;
	private UserResponse creator;
	private FolderResponse folder;
	private Integer vocabularyCount;
}