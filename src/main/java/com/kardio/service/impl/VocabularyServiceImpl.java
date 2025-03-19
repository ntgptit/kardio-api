package com.kardio.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.vocabulary.VocabularyBulkImportRequest;
import com.kardio.dto.vocabulary.VocabularyBulkOperationResponse;
import com.kardio.dto.vocabulary.VocabularyCreateRequest;
import com.kardio.dto.vocabulary.VocabularyResponse;
import com.kardio.dto.vocabulary.VocabularyUpdateRequest;
import com.kardio.dto.vocabulary.VocabularyWithProgressResponse;
import com.kardio.entity.LearningProgress;
import com.kardio.entity.StarredItem;
import com.kardio.entity.StudyModule;
import com.kardio.entity.User;
import com.kardio.entity.Vocabulary;
import com.kardio.exception.KardioException;
import com.kardio.mapper.LearningProgressMapper;
import com.kardio.mapper.VocabularyMapper;
import com.kardio.repository.LearningProgressRepository;
import com.kardio.repository.StarredItemRepository;
import com.kardio.repository.StudyModuleRepository;
import com.kardio.repository.UserRepository;
import com.kardio.repository.VocabularyRepository;
import com.kardio.service.VocabularyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of VocabularyService for vocabulary management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VocabularyServiceImpl implements VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final StudyModuleRepository studyModuleRepository;
    private final UserRepository userRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final StarredItemRepository starredItemRepository;

    private final VocabularyMapper vocabularyMapper;
    private final LearningProgressMapper learningProgressMapper;

    @Override
    @Transactional
    public VocabularyResponse createVocabulary(VocabularyCreateRequest request) {
        log.info("Creating vocabulary with term: {}", request.getTerm());

        // Validate and get study module
        final StudyModule module = studyModuleRepository.findById(request.getModuleId()).orElseThrow(() -> {
            log.error("Study module not found with ID: {}", request.getModuleId());
            return KardioException.resourceNotFound("StudyModule", request.getModuleId());
        });

        // Create vocabulary entity
        final Vocabulary vocabulary = vocabularyMapper.createFromRequest(request, module);
        final Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);

        log.info("Vocabulary created successfully with ID: {}", savedVocabulary.getId());
        return vocabularyMapper.toDto(savedVocabulary);
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyResponse getVocabularyById(UUID id) {
        log.debug("Getting vocabulary by ID: {}", id);

        final Vocabulary vocabulary = findVocabularyById(id);
        return vocabularyMapper.toDto(vocabulary);
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyWithProgressResponse getVocabularyWithProgress(UUID id, UUID userId) {
        log.debug("Getting vocabulary with progress - vocabulary ID: {}, user ID: {}", id, userId);

        final Vocabulary vocabulary = findVocabularyById(id);
        LearningProgress progress = null;
        boolean isStarred = false;

        if (userId != null) {
            // Get user progress
            progress = learningProgressRepository.findByVocabularyIdAndUserId(id, userId).orElse(null);

            // Check if vocabulary is starred
            isStarred = starredItemRepository.existsByUserIdAndVocabularyId(userId, id);
        }

        return vocabularyMapper.toWithProgressResponse(vocabulary, progress, isStarred);
    }

    @Override
    @Transactional
    public VocabularyResponse updateVocabulary(UUID id, VocabularyUpdateRequest request) {
        log.info("Updating vocabulary with ID: {}", id);

        final Vocabulary vocabulary = findVocabularyById(id);
        final Vocabulary updatedVocabulary = vocabularyMapper.updateFromRequest(request, vocabulary);
        final Vocabulary savedVocabulary = vocabularyRepository.save(updatedVocabulary);

        log.info("Vocabulary updated successfully: {}", savedVocabulary.getId());
        return vocabularyMapper.toDto(savedVocabulary);
    }

    @Override
    @Transactional
    public SuccessResponse deleteVocabulary(UUID id) {
        log.info("Deleting vocabulary with ID: {}", id);

        final Vocabulary vocabulary = findVocabularyById(id);
        vocabulary.softDelete();
        vocabularyRepository.save(vocabulary);

        log.info("Vocabulary deleted successfully: {}", id);
        return SuccessResponse.of("Vocabulary deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VocabularyResponse> getVocabulariesByModule(UUID moduleId, Pageable pageable) {
        log.debug("Getting vocabularies by module ID: {} with pagination: {}", moduleId, pageable);

        // Validate module exists
        if (!studyModuleRepository.existsById(moduleId)) {
            log.error("Study module not found with ID: {}", moduleId);
            throw KardioException.resourceNotFound("StudyModule", moduleId);
        }

        // Get vocabularies with pagination
        final Page<Vocabulary> vocabularyPage = vocabularyRepository.findByModuleId(moduleId, pageable);
        final Page<VocabularyResponse> dtoPage = vocabularyPage.map(vocabularyMapper::toDto);

        return createPageResponse(dtoPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public
            PageResponse<VocabularyWithProgressResponse>
            getVocabulariesByModuleWithProgress(UUID moduleId, UUID userId, Pageable pageable) {
        log.debug("Getting vocabularies by module ID: {} with progress for user ID: {}", moduleId, userId);

        // Validate module exists
        if (!studyModuleRepository.existsById(moduleId)) {
            log.error("Study module not found with ID: {}", moduleId);
            throw KardioException.resourceNotFound("StudyModule", moduleId);
        }

        // Validate user exists
        final User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found with ID: {}", userId);
            return KardioException.resourceNotFound("User", userId);
        });

        // Get vocabularies with pagination
        final Page<Vocabulary> vocabularyPage = vocabularyRepository.findByModuleId(moduleId, pageable);

        // Get learning progress for these vocabularies
        final List<UUID> vocabularyIds = vocabularyPage
            .getContent()
            .stream()
            .map(Vocabulary::getId)
            .collect(Collectors.toList());

        final List<LearningProgress> progressList = learningProgressRepository
            .findByUserIdAndVocabularyIdIn(userId, vocabularyIds);

        // Create map of vocabulary ID to progress
        final Map<UUID, LearningProgress> progressMap = progressList
            .stream()
            .collect(Collectors.toMap(p -> p.getVocabulary().getId(), Function.identity()));

        // Get starred items
        final List<StarredItem> starredItems = starredItemRepository
            .findByUserIdAndVocabularyIdIn(userId, vocabularyIds);

        final Set<UUID> starredIds = starredItems
            .stream()
            .map(item -> item.getVocabulary().getId())
            .collect(Collectors.toSet());

        // Map to DTOs with progress
        final Page<VocabularyWithProgressResponse> dtoPage = vocabularyPage.map(vocabulary -> {
            final UUID vocabId = vocabulary.getId();
            final LearningProgress progress = progressMap.get(vocabId);
            final boolean isStarred = starredIds.contains(vocabId);

            return vocabularyMapper.toWithProgressResponse(vocabulary, progress, isStarred);
        });

        return createPageResponse(dtoPage, pageable);
    }

    @Override
    @Transactional
    public VocabularyBulkOperationResponse bulkImportVocabularies(VocabularyBulkImportRequest request) {
        log.info("Bulk importing {} vocabularies to module ID: {}", request.getItems().length, request.getModuleId());

        // Validate and get study module
        final StudyModule module = studyModuleRepository.findById(request.getModuleId()).orElseThrow(() -> {
            log.error("Study module not found with ID: {}", request.getModuleId());
            return KardioException.resourceNotFound("StudyModule", request.getModuleId());
        });

        int successCount = 0;
        int failCount = 0;
        final List<String> errors = new ArrayList<>();

        // Process each vocabulary item
        for (VocabularyCreateRequest item : request.getItems()) {
            try {
                // Validate required fields
                if (!StringUtils.hasText(item.getTerm()) || !StringUtils.hasText(item.getDefinition())) {
                    throw new IllegalArgumentException("Term and definition are required");
                }

                // Create vocabulary entity
                final Vocabulary vocabulary = vocabularyMapper.createFromRequest(item, module);
                vocabularyRepository.save(vocabulary);
                successCount++;

            } catch (Exception e) {
                log.error("Failed to import vocabulary term '{}': {}", item.getTerm(), e.getMessage());
                failCount++;
                errors.add("Failed to import term '" + item.getTerm() + "': " + e.getMessage());
            }
        }

        log.info("Bulk import completed. Success: {}, Failed: {}", successCount, failCount);

        return VocabularyBulkOperationResponse
            .builder()
            .successCount(successCount)
            .failCount(failCount)
            .errors(errors.toArray(new String[0]))
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VocabularyResponse> getStarredVocabularies(UUID userId, Pageable pageable) {
        log.debug("Getting starred vocabularies for user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound("User", userId);
        }

        // Get starred vocabularies with pagination
        final Page<Vocabulary> vocabularyPage = vocabularyRepository.findStarredByUserId(userId, pageable);
        final Page<VocabularyResponse> dtoPage = vocabularyPage.map(vocabulary -> {
            VocabularyResponse response = vocabularyMapper.toDto(vocabulary);
            response.setIsStarred(true); // These are starred by definition
            return response;
        });

        return createPageResponse(dtoPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VocabularyResponse> searchVocabularies(String term, Pageable pageable) {
        log.debug("Searching vocabularies with term: {}", term);

        if (!StringUtils.hasText(term) || term.length() < 2) {
            throw new KardioException("Search term must be at least 2 characters", HttpStatus.BAD_REQUEST);
        }

        // Search vocabularies with pagination
        final Page<Vocabulary> vocabularyPage = vocabularyRepository.findByTermContainingIgnoreCase(term, pageable);
        final Page<VocabularyResponse> dtoPage = vocabularyPage.map(vocabularyMapper::toDto);

        return createPageResponse(dtoPage, pageable);
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
     * Helper method to find vocabulary by ID or throw exception.
     */
    private Vocabulary findVocabularyById(UUID id) {
        return vocabularyRepository.findById(id).orElseThrow(() -> {
            log.error("Vocabulary not found with ID: {}", id);
            return KardioException.resourceNotFound("Vocabulary", id);
        });
    }
}