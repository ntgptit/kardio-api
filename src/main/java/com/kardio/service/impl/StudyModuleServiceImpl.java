package com.kardio.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.module.StudyModuleCreateRequest;
import com.kardio.dto.module.StudyModuleDetailedResponse;
import com.kardio.dto.module.StudyModuleResponse;
import com.kardio.dto.module.StudyModuleShareRequest;
import com.kardio.dto.module.StudyModuleSummaryResponse;
import com.kardio.dto.module.StudyModuleUpdateRequest;
import com.kardio.entity.Folder;
import com.kardio.entity.LearningProgress;
import com.kardio.entity.SharedStudyModule;
import com.kardio.entity.StudyModule;
import com.kardio.entity.User;
import com.kardio.entity.Vocabulary;
import com.kardio.entity.enums.LearningStatus;
import com.kardio.entity.enums.VisibilityType;
import com.kardio.exception.KardioException;
import com.kardio.mapper.StudyModuleMapper;
import com.kardio.repository.FolderRepository;
import com.kardio.repository.LearningProgressRepository;
import com.kardio.repository.SharedStudyModuleRepository;
import com.kardio.repository.StudyModuleRepository;
import com.kardio.repository.UserRepository;
import com.kardio.repository.VocabularyRepository;
import com.kardio.service.StudyModuleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of StudyModuleService for module management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudyModuleServiceImpl implements StudyModuleService {

    private final StudyModuleRepository studyModuleRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final SharedStudyModuleRepository sharedStudyModuleRepository;

    private final StudyModuleMapper studyModuleMapper;

    @Override
    @Transactional
    public StudyModuleResponse createModule(StudyModuleCreateRequest request, UUID creatorId) {
        log.info("Creating study module with name: {} for user ID: {}", request.getName(), creatorId);

        // Validate and get creator
        User creator = userRepository.findById(creatorId).orElseThrow(() -> {
            log.error("User not found with ID: {}", creatorId);
            return KardioException.resourceNotFound("User", creatorId);
        });

        // Get folder if provided
        Folder folder = null;
        if (request.getFolderId() != null) {
            folder = folderRepository.findById(request.getFolderId()).orElseThrow(() -> {
                log.error("Folder not found with ID: {}", request.getFolderId());
                return KardioException.resourceNotFound("Folder", request.getFolderId());
            });

            // Check folder ownership
            if (!folder.getUser().getId().equals(creatorId)) {
                log.error("User {} does not own folder {}", creatorId, request.getFolderId());
                throw KardioException.forbidden("You do not have permission to use this folder");
            }
        }

        // Create module entity
        StudyModule module = studyModuleMapper.createFromRequest(request, creator, folder);
        StudyModule savedModule = studyModuleRepository.save(module);

        log.info("Study module created successfully with ID: {}", savedModule.getId());
        return studyModuleMapper.toDto(savedModule);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyModuleResponse getModuleById(UUID id, UUID userId) {
        log.debug("Getting study module by ID: {} for user ID: {}", id, userId);

        StudyModule module = findModuleById(id);

        // Check access
        if (!canAccessModule(module, userId)) {
            log.error("User {} does not have access to module {}", userId, id);
            throw KardioException.forbidden("You do not have permission to access this module");
        }

        return studyModuleMapper.toDto(module);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyModuleDetailedResponse getModuleDetailedById(UUID id, UUID userId) {
        log.debug("Getting detailed study module by ID: {} for user ID: {}", id, userId);

        StudyModule module = findModuleById(id);

        // Check access
        if (!canAccessModule(module, userId)) {
            log.error("User {} does not have access to module {}", userId, id);
            throw KardioException.forbidden("You do not have permission to access this module");
        }

        // Get vocabulary count
        int vocabularyCount = (int) vocabularyRepository.countByModuleId(id);

        // Calculate statistics if user is provided
        double averageAccuracy = 0.0;
        double completionPercentage = 0.0;

        if (userId != null && vocabularyCount > 0) {
            // Count mastered vocabularies
            long masteredCount = learningProgressRepository
                .countByUserIdAndVocabulary_Module_IdAndStatus(userId, id, LearningStatus.MASTERED);

            // Count learning vocabularies
            long learningCount = learningProgressRepository
                .countByUserIdAndVocabulary_Module_IdAndStatus(userId, id, LearningStatus.LEARNING);

            // Calculate completion percentage
            completionPercentage = (double) masteredCount / vocabularyCount * 100;

            // Get all progress for this module's vocabularies
            List<Vocabulary> vocabularies = vocabularyRepository.findByModuleId(id, Pageable.unpaged()).getContent();
            List<UUID> vocabularyIds = vocabularies.stream().map(Vocabulary::getId).collect(Collectors.toList());

            List<LearningProgress> progressList = learningProgressRepository
                .findByUserIdAndVocabularyIdIn(userId, vocabularyIds);

            // Calculate average accuracy
            if (!progressList.isEmpty()) {
                averageAccuracy = progressList
                    .stream()
                    .mapToDouble(LearningProgress::getAccuracyRate)
                    .average()
                    .orElse(0.0);
            }
        }

        return studyModuleMapper.toDetailedResponse(module, vocabularyCount, averageAccuracy, completionPercentage);
    }

    @Override
    @Transactional
    public StudyModuleResponse updateModule(UUID id, StudyModuleUpdateRequest request, UUID userId) {
        log.info("Updating study module with ID: {} by user ID: {}", id, userId);

        StudyModule module = findModuleById(id);

        // Check ownership
        if (!module.getCreator().getId().equals(userId)) {
            log.error("User {} is not the owner of module {}", userId, id);
            throw KardioException.forbidden("You must be the owner to update this module");
        }

        // Get folder if provided
        Folder folder = null;
        if (request.getFolderId() != null) {
            folder = folderRepository.findById(request.getFolderId()).orElseThrow(() -> {
                log.error("Folder not found with ID: {}", request.getFolderId());
                return KardioException.resourceNotFound("Folder", request.getFolderId());
            });

            // Check folder ownership
            if (!folder.getUser().getId().equals(userId)) {
                log.error("User {} does not own folder {}", userId, request.getFolderId());
                throw KardioException.forbidden("You do not have permission to use this folder");
            }
        }

        // Update module
        StudyModule updatedModule = studyModuleMapper.updateFromRequest(request, module, folder);
        StudyModule savedModule = studyModuleRepository.save(updatedModule);

        log.info("Study module updated successfully: {}", savedModule.getId());
        return studyModuleMapper.toDto(savedModule);
    }

    @Override
    @Transactional
    public SuccessResponse deleteModule(UUID id, UUID userId) {
        log.info("Deleting study module with ID: {} by user ID: {}", id, userId);

        StudyModule module = findModuleById(id);

        // Check ownership
        if (!module.getCreator().getId().equals(userId)) {
            log.error("User {} is not the owner of module {}", userId, id);
            throw KardioException.forbidden("You must be the owner to delete this module");
        }

        // Soft delete
        module.softDelete();
        studyModuleRepository.save(module);

        log.info("Study module deleted successfully: {}", id);
        return SuccessResponse.of("Study module deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudyModuleSummaryResponse> getModulesByUser(UUID userId, Pageable pageable) {
        log.debug("Getting modules by user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound("User", userId);
        }

        Page<StudyModule> modulePage = studyModuleRepository.findByCreatorId(userId, pageable);

        return createSummaryPageResponse(modulePage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudyModuleSummaryResponse> getModulesByFolder(UUID folderId, UUID userId, Pageable pageable) {
        log.debug("Getting modules by folder ID: {} for user ID: {}", folderId, userId);

        // Validate folder exists and user can access it
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> {
            log.error("Folder not found with ID: {}", folderId);
            return KardioException.resourceNotFound("Folder", folderId);
        });

        // Check folder ownership
        if (!folder.getUser().getId().equals(userId)) {
            log.error("User {} does not own folder {}", userId, folderId);
            throw KardioException.forbidden("You do not have permission to access this folder");
        }

        Page<StudyModule> modulePage = studyModuleRepository.findByFolderId(folderId, pageable);

        return createSummaryPageResponse(modulePage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudyModuleSummaryResponse> getPublicModules(Pageable pageable) {
        log.debug("Getting public modules");

        Page<StudyModule> modulePage = studyModuleRepository.findByVisibility(VisibilityType.PUBLIC, pageable);

        return createSummaryPageResponse(modulePage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudyModuleSummaryResponse> getSharedModules(UUID userId, Pageable pageable) {
        log.debug("Getting modules shared with user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound("User", userId);
        }

        Page<StudyModule> modulePage = studyModuleRepository.findSharedWithUser(userId, pageable);

        return createSummaryPageResponse(modulePage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public
            PageResponse<StudyModuleSummaryResponse>
            searchModules(String term, boolean publicOnly, UUID userId, Pageable pageable) {
        log.debug("Searching modules with term: {}, publicOnly: {}, userId: {}", term, publicOnly, userId);

        if (!StringUtils.hasText(term) || term.length() < 2) {
            throw new KardioException("Search term must be at least 2 characters", HttpStatus.BAD_REQUEST);
        }

        Page<StudyModule> modulePage;

        if (publicOnly) {
            // Search only public modules
            modulePage = studyModuleRepository.searchPublicByNameOrDescription(term, VisibilityType.PUBLIC, pageable);
        } else if (userId != null) {
            // Search modules accessible to user
            // This would be a more complex query that includes:
            // - Modules created by the user
            // - Public modules
            // - Modules shared with the user
            // Search modules accessible to user
            Page<StudyModule> allModulesPage = studyModuleRepository.searchByNameOrDescription(term, pageable);

            // Lọc kết quả theo quyền truy cập
            List<StudyModule> accessibleModules = allModulesPage
                .getContent()
                .stream()
                .filter(module -> canAccessModule(module, userId))
                .collect(Collectors.toList());

            // Tạo một Page mới từ danh sách đã lọc
            modulePage = new PageImpl<>(accessibleModules, pageable, accessibleModules.size());
        } else {
            // Search all modules (admin only)
            modulePage = studyModuleRepository.searchByNameOrDescription(term, pageable);
        }

        return createSummaryPageResponse(modulePage, pageable);
    }

    @Override
    @Transactional
    public SuccessResponse shareModule(UUID id, StudyModuleShareRequest request, UUID ownerId) {
        log.info("Sharing module ID: {} by owner ID: {} with {} users", id, ownerId, request.getUserIds().length);

        StudyModule module = findModuleById(id);

        // Check ownership
        if (!module.getCreator().getId().equals(ownerId)) {
            log.error("User {} is not the owner of module {}", ownerId, id);
            throw KardioException.forbidden("You must be the owner to share this module");
        }

        // Verify module is sharable
        if (module.getVisibility() == VisibilityType.PRIVATE) {
            // Update visibility to SHARED
            module.setVisibility(VisibilityType.SHARED);
            studyModuleRepository.save(module);
        }

        int successCount = 0;

        // Share with each user
        for (UUID userId : request.getUserIds()) {
            try {
                // Skip if sharing with self
                if (userId.equals(ownerId)) {
                    continue;
                }

                // Check if user exists
                User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> KardioException.resourceNotFound("User", userId));

                // Check if already shared
                if (!sharedStudyModuleRepository.existsByStudyModuleIdAndUserId(id, userId)) {
                    // Create sharing record
                    SharedStudyModule sharedModule = SharedStudyModule.builder().studyModule(module).user(user).build();

                    sharedStudyModuleRepository.save(sharedModule);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Failed to share module {} with user {}: {}", id, userId, e.getMessage());
                // Continue with next user
            }
        }

        log.info("Module shared successfully with {} users", successCount);
        return SuccessResponse.of("Module shared successfully with " + successCount + " users");
    }

    @Override
    @Transactional
    public SuccessResponse unshareModule(UUID id, UUID userId, UUID ownerId) {
        log.info("Unsharing module ID: {} from user ID: {} by owner ID: {}", id, userId, ownerId);

        StudyModule module = findModuleById(id);

        // Check ownership
        if (!module.getCreator().getId().equals(ownerId)) {
            log.error("User {} is not the owner of module {}", ownerId, id);
            throw KardioException.forbidden("You must be the owner to unshare this module");
        }

        // Delete sharing record
        sharedStudyModuleRepository.deleteByStudyModuleIdAndUserId(id, userId);

        // If no more shares, update visibility back to PRIVATE if currently SHARED
        if (module.getVisibility() == VisibilityType.SHARED) {
            long shareCount = sharedStudyModuleRepository.countByStudyModuleId(id);

            if (shareCount == 0) {
                module.setVisibility(VisibilityType.PRIVATE);
                studyModuleRepository.save(module);
            }
        }

        log.info("Module unshared successfully from user {}", userId);
        return SuccessResponse.of("Module unshared successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyModuleSummaryResponse> getRecentModules(UUID userId, int limit) {
        log.debug("Getting {} recent modules for user ID: {}", limit, userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound("User", userId);
        }

        Pageable limitedRequest = PageRequest.of(0, limit);
        List<StudyModule> modules = studyModuleRepository.findRecentModules(userId, limitedRequest);

        return modules.stream().map(module -> {
            int vocabularyCount = (int) vocabularyRepository.countByModuleId(module.getId());
            return studyModuleMapper.toSummaryResponse(module, vocabularyCount);
        }).collect(Collectors.toList());
    }

    /**
     * Helper method to create summary page response from page object.
     */
    private
            PageResponse<StudyModuleSummaryResponse>
            createSummaryPageResponse(Page<StudyModule> modulePage, Pageable pageable) {

        // Get vocabulary counts for these modules
        List<UUID> moduleIds = modulePage.getContent().stream().map(StudyModule::getId).collect(Collectors.toList());

        // This would ideally be a single query to get counts for all modules at once
        Map<UUID, Integer> vocabularyCounts = moduleIds
            .stream()
            .collect(Collectors.toMap(id -> id, id -> (int) vocabularyRepository.countByModuleId(id)));

        // Map to summary responses
        Page<StudyModuleSummaryResponse> dtoPage = modulePage.map(module -> {
            Integer count = vocabularyCounts.getOrDefault(module.getId(), 0);
            return studyModuleMapper.toSummaryResponse(module, count);
        });

        return PageResponse
            .<StudyModuleSummaryResponse>builder()
            .content(dtoPage.getContent())
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(dtoPage.getTotalElements())
            .totalPages(dtoPage.getTotalPages())
            .first(dtoPage.isFirst())
            .last(dtoPage.isLast())
            .build();
    }

    /**
     * Helper method to find module by ID or throw exception.
     */
    private StudyModule findModuleById(UUID id) {
        return studyModuleRepository.findById(id).orElseThrow(() -> {
            log.error("Study module not found with ID: {}", id);
            return KardioException.resourceNotFound("StudyModule", id);
        });
    }

    /**
     * Checks if a user can access a module.
     */
    private boolean canAccessModule(StudyModule module, UUID userId) {
        // Admin access or public module
        if (userId == null || module.getVisibility() == VisibilityType.PUBLIC) {
            return true;
        }

        // Owner access
        if (module.getCreator().getId().equals(userId)) {
            return true;
        }

        // Shared access
        if (module.getVisibility() == VisibilityType.SHARED) {
            return sharedStudyModuleRepository.existsByStudyModuleIdAndUserId(module.getId(), userId);
        }

        return false;
    }
}