package com.kardio.dto.folder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.kardio.dto.module.StudyModuleSummaryResponse;
import com.kardio.dto.user.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for folder with subfolder list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDetailedResponse {
	private UUID id;
	private String name;
	private String description;
	private UUID parentFolderId;
	private String parentFolderName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<FolderResponse> subfolders;
	private List<StudyModuleSummaryResponse> modules;
	private UserResponse user;
}