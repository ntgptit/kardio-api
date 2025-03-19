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
 * DTO for detailed study module response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyModuleDetailedResponse {
	private UUID id;
	private String name;
	private String description;
	private VisibilityType visibility;
	private LocalDateTime lastStudiedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
	private UserResponse creator;
	private FolderResponse folder;
	private Integer vocabularyCount;
	private Double averageAccuracy;
	private Double completionPercentage;
}
