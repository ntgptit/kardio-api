package com.kardio.dto.folder;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for moving folders between parent folders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderMoveRequest {
	private UUID targetParentFolderId;
}
