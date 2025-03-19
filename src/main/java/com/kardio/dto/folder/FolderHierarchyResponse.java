package com.kardio.dto.folder;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for folder hierarchy (tree structure)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderHierarchyResponse {
	private UUID id;
	private String name;
	private UUID parentFolderId;
	private List<FolderHierarchyResponse> children;
	private Integer moduleCount;
}
