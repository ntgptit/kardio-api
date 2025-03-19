package com.kardio.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.folder.FolderCreateRequest;
import com.kardio.dto.folder.FolderDetailedResponse;
import com.kardio.dto.folder.FolderHierarchyResponse;
import com.kardio.dto.folder.FolderMoveRequest;
import com.kardio.dto.folder.FolderResponse;
import com.kardio.dto.folder.FolderUpdateRequest;

/**
 * Service interface for folder management.
 */
public interface FolderService {

    /**
     * Creates a new folder.
     *
     * @param request The folder creation request
     * @param userId  The ID of the user creating the folder
     * @return The created folder
     */
    FolderResponse createFolder(FolderCreateRequest request, UUID userId);

    /**
     * Gets a folder by ID.
     *
     * @param id     Folder ID
     * @param userId User ID for ownership check
     * @return The folder response
     */
    FolderResponse getFolderById(UUID id, UUID userId);

    /**
     * Gets detailed folder information by ID with subfolders and modules.
     *
     * @param id     Folder ID
     * @param userId User ID for ownership check
     * @return The detailed folder response
     */
    FolderDetailedResponse getFolderDetailedById(UUID id, UUID userId);

    /**
     * Updates a folder.
     *
     * @param id      Folder ID
     * @param request The update request
     * @param userId  User ID for ownership check
     * @return The updated folder
     */
    FolderResponse updateFolder(UUID id, FolderUpdateRequest request, UUID userId);

    /**
     * Deletes a folder (soft delete).
     *
     * @param id     Folder ID
     * @param userId User ID for ownership check
     * @return Success response
     */
    SuccessResponse deleteFolder(UUID id, UUID userId);

    /**
     * Gets all root folders for a user.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Paginated list of root folders
     */
    PageResponse<FolderResponse> getRootFolders(UUID userId, Pageable pageable);

    /**
     * Gets all subfolders for a folder.
     *
     * @param parentId Parent folder ID
     * @param userId   User ID for ownership check
     * @param pageable Pagination information
     * @return Paginated list of subfolders
     */
    PageResponse<FolderResponse> getSubfolders(UUID parentId, UUID userId, Pageable pageable);

    /**
     * Gets the complete folder hierarchy for a user.
     *
     * @param userId User ID
     * @return The folder hierarchy
     */
    List<FolderHierarchyResponse> getFolderHierarchy(UUID userId);

    /**
     * Moves a folder to another parent folder.
     *
     * @param id      Folder ID
     * @param request The move request
     * @param userId  User ID for ownership check
     * @return The updated folder
     */
    FolderResponse moveFolder(UUID id, FolderMoveRequest request, UUID userId);
}