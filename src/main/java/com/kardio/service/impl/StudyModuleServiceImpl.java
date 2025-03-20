package com.kardio.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.module.StudyModuleCreateRequest;
import com.kardio.dto.module.StudyModuleDetailedResponse;
import com.kardio.dto.module.StudyModuleResponse;
import com.kardio.dto.module.StudyModuleShareRequest;
import com.kardio.dto.module.StudyModuleSummaryResponse;
import com.kardio.dto.module.StudyModuleUpdateRequest;
import com.kardio.entity.Folder;
import com.kardio.entity.SharedStudyModule;
import com.kardio.entity.StudyModule;
import com.kardio.entity.User;
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
    private final MessageSource messageSource;
    private final StudyModuleMapper studyModuleMapper;

    @Override
    @Transactional
    public StudyModuleResponse createModule(StudyModuleCreateRequest request, UUID creatorId) {
        log.info("Creating study module with name: {} for user ID: {}", request.getName(), creatorId);

        // Validate and get creator
        User creator = userRepository.findById(creatorId).orElseThrow(() -> {
            log.error("User not found with ID: {}", creatorId);
            return KardioException.resourceNotFound(messageSource, "entity.user", creatorId);
        });

        // Get folder if provided
        Folder folder = null;
        if (request.getFolderId() != null) {
            folder = folderRepository.findById(request.getFolderId()).orElseThrow(() -> {
                log.error("Folder not found with ID: {}", request.getFolderId());
                return KardioException.resourceNotFound(messageSource, "entity.folder", request.getFolderId());
            });

            // Check folder ownership
            if (!folder.getUser().getId().equals(creatorId)) {
                log.error("User {} does not own folder {}", creatorId, request.getFolderId());
                throw KardioException
                    .forbidden(
                        messageSource,
                        "error.forbidden.resource",
                        messageSource.getMessage("entity.folder", null, LocaleContextHolder.getLocale()));
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
    @Retryable(value = {
            Exception.class
    }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public StudyModuleResponse getModuleById(UUID id, UUID userId) {
        log.debug("Getting study module by ID: {} for user ID: {}", id, userId);

        StudyModule module = findModuleById(id);

        // Check access
        if (!canAccessModule(module, userId)) {
            log.error("User {} does not have access to module {}", userId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    "error.forbidden.resource",
                    messageSource.getMessage("entity.studyModule", null, LocaleContextHolder.getLocale()));
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
            throw KardioException
                .forbidden(
                    messageSource,
                    "error.forbidden.resource",
                    messageSource.getMessage("entity.studyModule", null, LocaleContextHolder.getLocale()));
        }

        // Get vocabulary count and statistics
        ModuleStatistics statistics = calculateModuleStatistics(id, userId);

        return studyModuleMapper
            .toDetailedResponse(
                module,
                statistics.getVocabularyCount(),
                statistics.getAverageAccuracy(),
                statistics.getCompletionPercentage());
    }

    @Override
    @Transactional
    public StudyModuleResponse updateModule(UUID id, StudyModuleUpdateRequest request, UUID userId) {
        log.info("Updating study module with ID: {} by user ID: {}", id, userId);

        StudyModule module = findModuleById(id);

        // Check ownership
        if (!module.getCreator().getId().equals(userId)) {
            log.error("User {} is not the owner of module {}", userId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    "error.forbidden.owner",
                    "update",
                    messageSource.getMessage("entity.studyModule", null, LocaleContextHolder.getLocale()));
        }

        // Get folder if provided
        Folder folder = getAndValidateFolderForUser(request.getFolderId(), userId);

        // Update module
        StudyModule updatedModule = studyModuleMapper.updateFromRequest(request, module, folder);
        StudyModule savedModule = studyModuleRepository.save(updatedModule);

        log.info("Study module updated successfully: {}", savedModule.getId());
        return studyModuleMapper.toDto(savedModule);
    }

    private Folder getAndValidateFolderForUser(UUID folderId, UUID userId) {
        if (folderId == null) {
            return null;
        }

        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> {
            log.error("Folder not found with ID: {}", folderId);
            return KardioException.resourceNotFound(messageSource, "entity.folder", folderId);
        });

        // Check folder ownership
        if (!folder.getUser().getId().equals(userId)) {
            log.error("User {} does not own folder {}", userId, folderId);
            throw KardioException
                .forbidden(
                    messageSource,
                    "error.forbidden.resource",
                    messageSource.getMessage("entity.folder", null, LocaleContextHolder.getLocale()));
        }

        return folder;
    }

    @Override
    @Transactional
    public SuccessResponse deleteModule(UUID id, UUID userId) {
        log.info("Deleting study module with ID: {} by user ID: {}", id, userId);

        StudyModule module = findModuleById(id);

        // Check ownership
        if (!module.getCreator().getId().equals(userId)) {
            log.error("User {} is not the owner of module {}", userId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    "error.forbidden.owner",
                    "delete",
                    messageSource.getMessage("entity.studyModule", null, LocaleContextHolder.getLocale()));
        }

        // Soft delete
        module.softDelete();
        studyModuleRepository.save(module);

        log.info("Study module deleted successfully: {}", id);
        return SuccessResponse.of(messageSource.getMessage("success.deleted", new Object[]{
                messageSource.getMessage("entity.studyModule", null, LocaleContextHolder.getLocale())
        }, LocaleContextHolder.getLocale()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudyModuleSummaryResponse> getModulesByUser(UUID userId, Pageable pageable) {
        log.debug("Getting modules by user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound(messageSource, "entity.user", userId);
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
            return KardioException.resourceNotFound(messageSource, "entity.folder", folderId);
        });

        // Check folder ownership
        if (!folder.getUser().getId().equals(userId)) {
            log.error("User {} does not own folder {}", userId, folderId);
            throw KardioException
                .forbidden(
                    messageSource,
                    "error.forbidden.resource",
                    messageSource.getMessage("entity.folder", null, LocaleContextHolder.getLocale()));
        }

        Page<StudyModule> modulePage = studyModuleRepository.findByFolderId(folderId, pageable);

        return createSummaryPageResponse(modulePage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "publicModules", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
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
            throw KardioException.resourceNotFound(messageSource, "entity.user", userId);
        }

        Page<StudyModule> modulePage = studyModuleRepository.findSharedWithUser(userId, pageable);

        return createSummaryPageResponse(modulePage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudyModuleSummaryResponse> searchModules(
            String term,
            boolean publicOnly,
            UUID userId,
            Pageable pageable) {
        log.debug("Searching modules with term: {}, publicOnly: {}, userId: {}", term, publicOnly, userId);

        // Validate search term
        if (StringUtils.isEmpty(term) || term.length() < 2) {
            throw KardioException.validationError(messageSource, "error.validation.searchterm", 2);
        }

        // Get appropriate page of modules based on search criteria
        Page<StudyModule> modulePage = getSearchModulesPage(term, publicOnly, userId, pageable);

        return createSummaryPageResponse(modulePage, pageable);
    }

    private Page<StudyModule> getSearchModulesPage(String term, boolean publicOnly, UUID userId, Pageable pageable) {
        if (publicOnly) {
            // Search only public modules
            return studyModuleRepository.searchPublicByNameOrDescription(term, VisibilityType.PUBLIC, pageable);
        } else if (userId != null) {
            // Filter accessible modules after search
            Page<StudyModule> allModulesPage = studyModuleRepository.searchByNameOrDescription(term, pageable);
            List<StudyModule> accessibleModules = allModulesPage
                .getContent()
                .stream()
                .filter(module -> canAccessModule(module, userId))
                .collect(Collectors.toList());

            return new PageImpl<>(accessibleModules, pageable, accessibleModules.size());
        } else {
            // Search all modules (admin only)
            return studyModuleRepository.searchByNameOrDescription(term, pageable);
        }
    }

    @Override
    @Transactional
    public SuccessResponse shareModule(UUID id, StudyModuleShareRequest request, UUID ownerId) {
        log.info("Sharing module ID: {} by owner ID: {} with {} users", id, ownerId, request.getUserIds().length);

        StudyModule module = findModuleById(id);

        // Check ownership
        if (!module.getCreator().getId().equals(ownerId)) {
            log.error("User {} is not the owner of module {}", ownerId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    "error.forbidden.owner",
                    "share",
                    messageSource.getMessage("entity.studyModule", null, LocaleContextHolder.getLocale()));
        }

        // Update module visibility if private
        updateModuleVisibilityIfNeeded(module);

        // Share with each user
        int successCount = shareModuleWithUsers(module, request.getUserIds(), ownerId);

        log.info("Module shared successfully with {} users", successCount);
        return SuccessResponse.of(messageSource.getMessage("success.shared", new Object[]{
                messageSource.getMessage("entity.studyModule", null, LocaleContextHolder.getLocale()), successCount
        }, LocaleContextHolder.getLocale()));
    }

    private void updateModuleVisibilityIfNeeded(StudyModule module) {
        if (module.getVisibility() == VisibilityType.PRIVATE) {
            module.setVisibility(VisibilityType.SHARED);
            studyModuleRepository.save(module);
        }
    }

    private int shareModuleWithUsers(StudyModule module, UUID[] userIds, UUID ownerId) {
        int successCount = 0;

        for (UUID userId : userIds) {
            // Skip if sharing with self
            if (userId.equals(ownerId)) {
                continue;
            }

            try {
                // Check if user exists
                User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> KardioException.resourceNotFound(messageSource, "entity.user", userId));

                // Check if already shared
                if (!sharedStudyModuleRepository.existsByStudyModuleIdAndUserId(module.getId(), userId)) {
                    // Create sharing record
                    SharedStudyModule sharedModule = SharedStudyModule.builder().studyModule(module).user(user).build();

                    sharedStudyModuleRepository.save(sharedModule);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Failed to share module {} with user {}: {}", module.getId(), userId, e.getMessage());
                // Continue with next user
            }
        }

        return successCount;
    }

    @Override
    @Transactional
    public SuccessResponse unshareModule(UUID id, UUID userId, UUID ownerId) {
        log.info("Unsharing module ID: {} from user ID: {} by owner ID: {}", id, userId, ownerId);

        StudyModule module = findModuleById(id);

        // Check ownership
        if (!module.getCreator().getId().equals(ownerId)) {
            log.error("User {} is not the owner of module {}", ownerId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    "error.forbidden.owner",
                    "unshare",
                    messageSource.getMessage("entity.studyModule", null, LocaleContextHolder.getLocale()));
        }

        // Delete sharing record
        sharedStudyModuleRepository.deleteByStudyModuleIdAndUserId(id, userId);

        // Update visibility if needed
        updateModuleVisibilityAfterUnshare(module);

        log.info("Module unshared successfully from user {}", userId);
        return SuccessResponse.of(messageSource.getMessage("success.unshared", new Object[]{
                messageSource.getMessage("entity.studyModule", null, LocaleContextHolder.getLocale())
        }, LocaleContextHolder.getLocale()));
    }

    private void updateModuleVisibilityAfterUnshare(StudyModule module) {
        if (module.getVisibility() == VisibilityType.SHARED) {
            long shareCount = sharedStudyModuleRepository.countByStudyModuleId(module.getId());

            if (shareCount == 0) {
                module.setVisibility(VisibilityType.PRIVATE);
                studyModuleRepository.save(module);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "recentModules", key = "#userId + '-' + #limit")
    public List<StudyModuleSummaryResponse> getRecentModules(UUID userId, int limit) {
        log.debug("Getting {} recent modules for user ID: {}", limit, userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound(messageSource, "entity.user", userId);
        }

        Pageable limitedRequest = PageRequest.of(0, limit);
        List<StudyModule> modules = studyModuleRepository.findRecentModules(userId, limitedRequest);

        // Get vocabulary counts efficiently
        List<UUID> moduleIds = modules.stream().map(StudyModule::getId).collect(Collectors.toList());

        Map<UUID, Integer> vocabularyCounts = getVocabularyCounts(moduleIds);

        return modules
            .stream()
            .map(
                module -> studyModuleMapper.toSummaryResponse(module, vocabularyCounts.getOrDefault(module.getId(), 0)))
            .collect(Collectors.toList());
    }

    /**
     * Calculate statistics for a module
     */
    private ModuleStatistics calculateModuleStatistics(UUID moduleId, UUID userId) {
        // Get vocabulary count
        int vocabularyCount = (int) vocabularyRepository.countByModuleId(moduleId);

        double averageAccuracy = 0.0;
        double completionPercentage = 0.0;

        if (userId != null && vocabularyCount > 0) {
            // Get statistics in a single efficient query
            Map<String, Object> stats = learningProgressRepository.getModuleStatistics(moduleId, userId);

            long masteredCount = ((Number) stats.getOrDefault("masteredCount", 0L)).longValue();
            averageAccuracy = ((Number) stats.getOrDefault("averageAccuracy", 0.0)).doubleValue();

            // Calculate completion percentage
            completionPercentage = (double) masteredCount / vocabularyCount * 100;
        }

        return new ModuleStatistics(vocabularyCount, averageAccuracy, completionPercentage);
    }

    /**
     * Create summary page response from page of modules and vocabulary counts
     */
    private PageResponse<StudyModuleSummaryResponse> createSummaryPageResponse(
            Page<StudyModule> modulePage,
            Pageable pageable) {

        if (modulePage.isEmpty()) {
            return emptyPageResponse(pageable);
        }

        // Get module IDs efficiently
        List<UUID> moduleIds = modulePage.getContent().stream().map(StudyModule::getId).collect(Collectors.toList());

        // Get vocabulary counts in a single batch query
        Map<UUID, Integer> vocabularyCounts = getVocabularyCounts(moduleIds);

        // Map to summary responses with counts
        List<StudyModuleSummaryResponse> dtoList = modulePage
            .getContent()
            .stream()
            .map(
                module -> studyModuleMapper.toSummaryResponse(module, vocabularyCounts.getOrDefault(module.getId(), 0)))
            .collect(Collectors.toList());

        return PageResponse
            .<StudyModuleSummaryResponse>builder()
            .content(dtoList)
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(modulePage.getTotalElements())
            .totalPages(modulePage.getTotalPages())
            .first(modulePage.isFirst())
            .last(modulePage.isLast())
            .build();
    }

    /**
     * Get vocabulary counts for multiple modules in a single query
     */
    private Map<UUID, Integer> getVocabularyCounts(List<UUID> moduleIds) {
        if (moduleIds.isEmpty()) {
            return Map.of();
        }

        // Efficient batch query to get counts for all modules at once
        List<Object[]> countsData = vocabularyRepository.countVocabulariesForModules(moduleIds);

        return countsData
            .stream()
            .collect(
                Collectors
                    .toMap(
                        row -> (UUID) row[0],  // moduleId
                        row -> ((Number) row[1]).intValue(),  // count
                        (a, b) -> a  // In case of duplicates, keep first
                    ));
    }

    private <T> PageResponse<T> emptyPageResponse(Pageable pageable) {
        return PageResponse
            .<T>builder()
            .content(List.of())
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .build();
    }

    /**
     * Find module by ID or throw exception
     */
    private StudyModule findModuleById(UUID id) {
        return studyModuleRepository.findById(id).orElseThrow(() -> {
            log.error("Study module not found with ID: {}", id);
            return KardioException.resourceNotFound(messageSource, "entity.studyModule", id);
        });
    }

    /**
     * Check if a user can access a module
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

    /**
     * Private class to hold module statistics
     */
    private static class ModuleStatistics {
        private final int vocabularyCount;
        private final double averageAccuracy;
        private final double completionPercentage;

        public ModuleStatistics(int vocabularyCount, double averageAccuracy, double completionPercentage) {
            this.vocabularyCount = vocabularyCount;
            this.averageAccuracy = averageAccuracy;
            this.completionPercentage = completionPercentage;
        }

        public int getVocabularyCount() {
            return vocabularyCount;
        }

        public double getAverageAccuracy() {
            return averageAccuracy;
        }

        public double getCompletionPercentage() {
            return completionPercentage;
        }
    }
}