package com.kardio.dto.folder;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for folder response (without children)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderResponse {
	private UUID id;
	private String name;
	private String description;
	private UUID parentFolderId;
	private String parentFolderName;
	private LocalDateTime createdAt;
}
