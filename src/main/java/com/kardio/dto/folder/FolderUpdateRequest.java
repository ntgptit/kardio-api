package com.kardio.dto.folder;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a folder
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderUpdateRequest {
	private String name;
	private String description;
	private UUID parentFolderId;
}