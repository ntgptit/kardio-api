package com.kardio.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.module.StudyModuleCreateRequest;
import com.kardio.dto.module.StudyModuleDetailedResponse;
import com.kardio.dto.module.StudyModuleResponse;
import com.kardio.dto.module.StudyModuleShareRequest;
import com.kardio.dto.module.StudyModuleSummaryResponse;
import com.kardio.dto.module.StudyModuleUpdateRequest;

/**
 * Service interface for study module management.
 */
public interface StudyModuleService {

    /**
     * Creates a new study module.
     *
     * @param request   The module creation request
     * @param creatorId The ID of the user creating the module
     * @return The created module
     */
    StudyModuleResponse createModule(StudyModuleCreateRequest request, UUID creatorId);

    /**
     * Gets a module by ID.
     *
     * @param id     Module ID
     * @param userId User ID for access check (can be null for admin)
     * @return The module response
     */
    StudyModuleResponse getModuleById(UUID id, UUID userId);

    /**
     * Gets detailed module information by ID.
     *
     * @param id     Module ID
     * @param userId User ID for access check and progress calculation
     * @return The detailed module response
     */
    StudyModuleDetailedResponse getModuleDetailedById(UUID id, UUID userId);

    /**
     * Updates a module.
     *
     * @param id      Module ID
     * @param request The update request
     * @param userId  User ID for ownership check
     * @return The updated module
     */
    StudyModuleResponse updateModule(UUID id, StudyModuleUpdateRequest request, UUID userId);

    /**
     * Deletes a module (soft delete).
     *
     * @param id     Module ID
     * @param userId User ID for ownership check
     * @return Success response
     */
    SuccessResponse deleteModule(UUID id, UUID userId);

    /**
     * Gets all modules created by a user.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Paginated list of modules
     */
    PageResponse<StudyModuleSummaryResponse> getModulesByUser(UUID userId, Pageable pageable);

    /**
     * Gets all modules in a folder.
     *
     * @param folderId Folder ID
     * @param userId   User ID for access check
     * @param pageable Pagination information
     * @return Paginated list of modules
     */
    PageResponse<StudyModuleSummaryResponse> getModulesByFolder(UUID folderId, UUID userId, Pageable pageable);

    /**
     * Gets all public modules.
     *
     * @param pageable Pagination information
     * @return Paginated list of public modules
     */
    PageResponse<StudyModuleSummaryResponse> getPublicModules(Pageable pageable);

    /**
     * Gets modules shared with a user.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Paginated list of shared modules
     */
    PageResponse<StudyModuleSummaryResponse> getSharedModules(UUID userId, Pageable pageable);

    /**
     * Searches modules by term.
     *
     * @param term       Search term
     * @param publicOnly Whether to search only public modules
     * @param userId     User ID for visibility filtering (if publicOnly is false)
     * @param pageable   Pagination information
     * @return Paginated list of matching modules
     */
    PageResponse<StudyModuleSummaryResponse>
            searchModules(String term, boolean publicOnly, UUID userId, Pageable pageable);

    /**
     * Shares a module with users.
     *
     * @param id      Module ID
     * @param request The share request
     * @param ownerId ID of the module owner
     * @return Success response
     */
    SuccessResponse shareModule(UUID id, StudyModuleShareRequest request, UUID ownerId);

    /**
     * Unshares a module with a user.
     *
     * @param id      Module ID
     * @param userId  User ID to unshare with
     * @param ownerId ID of the module owner
     * @return Success response
     */
    SuccessResponse unshareModule(UUID id, UUID userId, UUID ownerId);

    /**
     * Gets recent modules for a user.
     *
     * @param userId User ID
     * @param limit  Maximum number of modules to return
     * @return List of recent modules
     */
    List<StudyModuleSummaryResponse> getRecentModules(UUID userId, int limit);
}