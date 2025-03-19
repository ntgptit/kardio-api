package com.kardio.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.folder.FolderCreateRequest;
import com.kardio.dto.folder.FolderDetailedResponse;
import com.kardio.dto.folder.FolderHierarchyResponse;
import com.kardio.dto.folder.FolderMoveRequest;
import com.kardio.dto.folder.FolderResponse;
import com.kardio.dto.folder.FolderUpdateRequest;
import com.kardio.dto.module.StudyModuleSummaryResponse;
import com.kardio.entity.Folder;
import com.kardio.entity.User;
import com.kardio.exception.KardioException;
import com.kardio.mapper.FolderMapper;
import com.kardio.mapper.StudyModuleMapper;
import com.kardio.repository.FolderRepository;
import com.kardio.repository.StudyModuleRepository;
import com.kardio.repository.UserRepository;
import com.kardio.service.FolderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of FolderService for folder management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final StudyModuleRepository studyModuleRepository;

    private final FolderMapper folderMapper;
    private final StudyModuleMapper studyModuleMapper;

    // Function to convert list of Object[] (from native query) to Map<UUID,
    // Integer>
    private final Function<List<Object[]>, Map<UUID, Integer>> moduleCountMapFunction;

    @Override
    @Transactional
    public FolderResponse createFolder(FolderCreateRequest request, UUID userId) {
        log.info("Creating folder with name: {} for user ID: {}", request.getName(), userId);

        // Validate and get user
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found with ID: {}", userId);
            return KardioException.resourceNotFound("User", userId);
        });

        // Get parent folder if provided
        Folder parentFolder = null;
        if (request.getParentFolderId() != null) {
            parentFolder = folderRepository.findById(request.getParentFolderId()).orElseThrow(() -> {
                log.error("Parent folder not found with ID: {}", request.getParentFolderId());
                return KardioException.resourceNotFound("Folder", request.getParentFolderId());
            });

            // Check parent folder ownership
            if (!parentFolder.getUser().getId().equals(userId)) {
                log.error("User {} does not own parent folder {}", userId, request.getParentFolderId());
                throw KardioException.forbidden("You do not have permission to use this parent folder");
            }
        }

        // Create folder entity
        Folder folder = folderMapper.createFromRequest(request, user, parentFolder);
        Folder savedFolder = folderRepository.save(folder);

        log.info("Folder created successfully with ID: {}", savedFolder.getId());
        return folderMapper.toDto(savedFolder);
    }

    @Override
    @Transactional(readOnly = true)
    public FolderResponse getFolderById(UUID id, UUID userId) {
        log.debug("Getting folder by ID: {} for user ID: {}", id, userId);

        Folder folder = findFolderByIdAndValidateOwnership(id, userId);
        return folderMapper.toDto(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public FolderDetailedResponse getFolderDetailedById(UUID id, UUID userId) {
        log.debug("Getting detailed folder by ID: {} for user ID: {}", id, userId);

        Folder folder = findFolderByIdAndValidateOwnership(id, userId);

        // Get subfolders
        List<Folder> subfolders = folderRepository.findByParentFolderId(id);
        List<FolderResponse> subfolderResponses = folderMapper.toDtoList(subfolders);

        // Get modules in this folder
        List<StudyModuleSummaryResponse> modules = studyModuleRepository
            .findByFolderId(id, Pageable.unpaged())
            .stream()
            .map(module -> {
                int vocabularyCount = (int) studyModuleRepository.countByFolderId(id);
                return studyModuleMapper.toSummaryResponse(module, vocabularyCount);
            })
            .collect(Collectors.toList());

        return folderMapper.toDetailedResponse(folder, subfolderResponses, modules);
    }

    @Override
    @Transactional
    public FolderResponse updateFolder(UUID id, FolderUpdateRequest request, UUID userId) {
        log.info("Updating folder with ID: {} by user ID: {}", id, userId);

        Folder folder = findFolderByIdAndValidateOwnership(id, userId);

        // Get parent folder if provided
        Folder parentFolder = null;
        if (request.getParentFolderId() != null) {
            // Prevent setting folder as its own parent
            if (request.getParentFolderId().equals(id)) {
                log.error("Cannot set folder as its own parent: {}", id);
                throw KardioException.validationError("A folder cannot be its own parent");
            }

            parentFolder = folderRepository.findById(request.getParentFolderId()).orElseThrow(() -> {
                log.error("Parent folder not found with ID: {}", request.getParentFolderId());
                return KardioException.resourceNotFound("Folder", request.getParentFolderId());
            });

            // Check parent folder ownership
            if (!parentFolder.getUser().getId().equals(userId)) {
                log.error("User {} does not own parent folder {}", userId, request.getParentFolderId());
                throw KardioException.forbidden("You do not have permission to use this parent folder");
            }

            // Prevent circular references
            if (isDescendantOf(parentFolder, folder)) {
                log.error("Circular reference detected: {} is a descendant of {}", request.getParentFolderId(), id);
                throw KardioException.validationError("Cannot create circular folder structure");
            }
        }

        // Update folder
        Folder updatedFolder = folderMapper.updateFromRequest(request, folder, parentFolder);
        Folder savedFolder = folderRepository.save(updatedFolder);

        log.info("Folder updated successfully: {}", savedFolder.getId());
        return folderMapper.toDto(savedFolder);
    }

    @Override
    @Transactional
    public SuccessResponse deleteFolder(UUID id, UUID userId) {
        log.info("Deleting folder with ID: {} by user ID: {}", id, userId);

        Folder folder = findFolderByIdAndValidateOwnership(id, userId);

        // Check if folder has modules
        long moduleCount = studyModuleRepository.countByFolderId(id);
        if (moduleCount > 0) {
            log.error("Cannot delete folder with modules: {}", id);
            throw KardioException
                .validationError("Cannot delete folder containing modules. Please move or delete the modules first.");
        }

        // Check if folder has subfolders
        List<Folder> subfolders = folderRepository.findByParentFolderId(id);
        if (!subfolders.isEmpty()) {
            log.error("Cannot delete folder with subfolders: {}", id);
            throw KardioException
                .validationError(
                    "Cannot delete folder containing subfolders. Please move or delete the subfolders first.");
        }

        // Soft delete
        folder.softDelete();
        folderRepository.save(folder);

        log.info("Folder deleted successfully: {}", id);
        return SuccessResponse.of("Folder deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FolderResponse> getRootFolders(UUID userId, Pageable pageable) {
        log.debug("Getting root folders for user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound("User", userId);
        }

        Page<Folder> folderPage = folderRepository.findByUserIdAndParentFolderIsNull(userId, pageable);
        Page<FolderResponse> dtoPage = folderPage.map(folderMapper::toDto);

        return createPageResponse(dtoPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FolderResponse> getSubfolders(UUID parentId, UUID userId, Pageable pageable) {
        log.debug("Getting subfolders for parent ID: {} and user ID: {}", parentId, userId);

        // Validate parent folder and ownership
        findFolderByIdAndValidateOwnership(parentId, userId);

        Page<Folder> folderPage = folderRepository.findByParentFolderId(parentId, pageable);
        Page<FolderResponse> dtoPage = folderPage.map(folderMapper::toDto);

        return createPageResponse(dtoPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderHierarchyResponse> getFolderHierarchy(UUID userId) {
        log.debug("Getting folder hierarchy for user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound("User", userId);
        }

        // Get all folders for the user
        List<Folder> allFolders = folderRepository.findAllByUserId(userId);

        // Get module counts for all folders
        List<Object[]> moduleCountResults = folderRepository.countModulesPerFolder(userId);
        Map<UUID, Integer> moduleCountMap = moduleCountMapFunction.apply(moduleCountResults);

        // Build the folder hierarchy
        return folderMapper.buildFolderHierarchy(allFolders, null, moduleCountMap);
    }

    @Override
    @Transactional
    public FolderResponse moveFolder(UUID id, FolderMoveRequest request, UUID userId) {
        log.info("Moving folder ID: {} to parent ID: {} by user ID: {}", id, request.getTargetParentFolderId(), userId);

        Folder folder = findFolderByIdAndValidateOwnership(id, userId);

        // If moving to root (null parent)
        if (request.getTargetParentFolderId() == null) {
            folder.setParentFolder(null);
            Folder savedFolder = folderRepository.save(folder);
            return folderMapper.toDto(savedFolder);
        }

        // Prevent moving to self
        if (request.getTargetParentFolderId().equals(id)) {
            log.error("Cannot move folder to itself: {}", id);
            throw KardioException.validationError("A folder cannot be its own parent");
        }

        // Get target parent folder
        Folder targetParent = folderRepository.findById(request.getTargetParentFolderId()).orElseThrow(() -> {
            log.error("Target parent folder not found with ID: {}", request.getTargetParentFolderId());
            return KardioException.resourceNotFound("Folder", request.getTargetParentFolderId());
        });

        // Check target parent ownership
        if (!targetParent.getUser().getId().equals(userId)) {
            log.error("User {} does not own target parent folder {}", userId, request.getTargetParentFolderId());
            throw KardioException.forbidden("You do not have permission to use this target parent folder");
        }

        // Prevent circular references
        if (isDescendantOf(targetParent, folder)) {
            log.error("Circular reference detected: {} is a descendant of {}", request.getTargetParentFolderId(), id);
            throw KardioException.validationError("Cannot create circular folder structure");
        }

        // Move folder
        folder.setParentFolder(targetParent);
        Folder savedFolder = folderRepository.save(folder);

        log.info("Folder moved successfully: {}", savedFolder.getId());
        return folderMapper.toDto(savedFolder);
    }

    /**
     * Helper method to create page response from page object.
     */
    private <T> PageResponse<T> createPageResponse(Page<T> page, Pageable pageable) {
        return PageResponse
            .<T>builder()
            .content(page.getContent())
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
    }

    /**
     * Helper method to find folder by ID and validate ownership.
     */
    private Folder findFolderByIdAndValidateOwnership(UUID id, UUID userId) {
        Folder folder = folderRepository.findById(id).orElseThrow(() -> {
            log.error("Folder not found with ID: {}", id);
            return KardioException.resourceNotFound("Folder", id);
        });

        // Check ownership
        if (!folder.getUser().getId().equals(userId)) {
            log.error("User {} does not own folder {}", userId, id);
            throw KardioException.forbidden("You do not have permission to access this folder");
        }

        return folder;
    }

    /**
     * Checks if potentialAncestor is an ancestor of folder (directly or
     * indirectly).
     * This prevents circular references when setting parent folders.
     */
    private boolean isDescendantOf(Folder potentialDescendant, Folder ancestor) {
        if (potentialDescendant == null) {
            return false;
        }

        if (potentialDescendant.getId().equals(ancestor.getId())) {
            return true;
        }

        return isDescendantOf(potentialDescendant.getParentFolder(), ancestor);
    }
}