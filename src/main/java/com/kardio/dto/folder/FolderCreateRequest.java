package com.kardio.dto.folder;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new folder
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderCreateRequest {
	@NotBlank(message = "Folder name is required")
	private String name;

	private String description;

	private UUID parentFolderId;
}
