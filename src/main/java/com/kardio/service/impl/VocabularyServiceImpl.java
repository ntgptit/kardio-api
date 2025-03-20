package com.kardio.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.vocabulary.VocabularyBulkImportRequest;
import com.kardio.dto.vocabulary.VocabularyBulkOperationResponse;
import com.kardio.dto.vocabulary.VocabularyCreateRequest;
import com.kardio.dto.vocabulary.VocabularyResponse;
import com.kardio.dto.vocabulary.VocabularyUpdateRequest;
import com.kardio.dto.vocabulary.VocabularyWithProgressResponse;
import com.kardio.entity.LearningProgress;
import com.kardio.entity.StudyModule;
import com.kardio.entity.Vocabulary;
import com.kardio.entity.enums.LearningStatus;
import com.kardio.exception.KardioException;
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

    // Constantes para mensagens de log e chaves de mensagens
    private static final String LOG_MODULE_NOT_FOUND = "Study module not found with ID: {}";
    private static final String KEY_ENTITY_MODULE = "entity.studyModule";
    private static final String KEY_ENTITY_USER = "entity.user";
    private static final String KEY_ENTITY_VOCABULARY = "entity.vocabulary";

    private final VocabularyRepository vocabularyRepository;
    private final StudyModuleRepository studyModuleRepository;
    private final UserRepository userRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final StarredItemRepository starredItemRepository;
    private final MessageSource messageSource;
    private final VocabularyMapper vocabularyMapper;

    @Override
    @Transactional
    public VocabularyResponse createVocabulary(VocabularyCreateRequest request) {
        log.info("Creating vocabulary with term: {}", request.getTerm());

        // Validate and get study module
        final StudyModule module = studyModuleRepository.findById(request.getModuleId()).orElseThrow(() -> {
            log.error(LOG_MODULE_NOT_FOUND, request.getModuleId());
            return KardioException.resourceNotFound(messageSource, KEY_ENTITY_MODULE, request.getModuleId());
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

        // Early return if no user ID provided
        if (userId == null) {
            return vocabularyMapper.toWithProgressResponse(vocabulary, null, false);
        }

        // Efficiently get user's learning progress and starred status in parallel
        final Optional<LearningProgress> progressOpt = learningProgressRepository
            .findByVocabularyIdAndUserId(id, userId);
        final boolean isStarred = starredItemRepository.existsByUserIdAndVocabularyId(userId, id);

        return vocabularyMapper.toWithProgressResponse(vocabulary, progressOpt.orElse(null), isStarred);
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
        return SuccessResponse.of(messageSource.getMessage("success.deleted", new Object[]{
                messageSource.getMessage(KEY_ENTITY_VOCABULARY, null, LocaleContextHolder.getLocale())
        }, LocaleContextHolder.getLocale()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VocabularyResponse> getVocabulariesByModule(UUID moduleId, Pageable pageable) {
        log.debug("Getting vocabularies by module ID: {} with pagination: {}", moduleId, pageable);

        // Validate module exists
        if (!studyModuleRepository.existsById(moduleId)) {
            log.error(LOG_MODULE_NOT_FOUND, moduleId);
            throw KardioException.resourceNotFound(messageSource, KEY_ENTITY_MODULE, moduleId);
        }

        // Get vocabularies with pagination
        final Page<Vocabulary> vocabularyPage = vocabularyRepository.findByModuleId(moduleId, pageable);
        final Page<VocabularyResponse> dtoPage = vocabularyPage.map(vocabularyMapper::toDto);

        return createPageResponse(dtoPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VocabularyWithProgressResponse> getVocabulariesByModuleWithProgress(
            UUID moduleId,
            UUID userId,
            Pageable pageable) {
        log.debug("Getting vocabularies by module ID: {} with progress for user ID: {}", moduleId, userId);

        // Validate module exists
        if (!studyModuleRepository.existsById(moduleId)) {
            log.error(LOG_MODULE_NOT_FOUND, moduleId);
            throw KardioException.resourceNotFound(messageSource, KEY_ENTITY_MODULE, moduleId);
        }

        // Validate user exists
        validateUser(userId);

        // Get vocabularies with progress and starred info in a single efficient query
        Page<Object[]> resultsPage = vocabularyRepository.findByModuleWithStats(moduleId, userId, pageable);

        // Map to DTOs with progress
        Page<VocabularyWithProgressResponse> dtoPage = resultsPage.map(row -> {
            Vocabulary vocabulary = (Vocabulary) row[0];
            boolean isStarred = (boolean) row[1];
            int correctCount = (int) row[2];
            int incorrectCount = (int) row[3];
            LearningStatus status = (LearningStatus) row[4];

            // Create a progress object if we have data
            LearningProgress progress = null;
            if (status != null) {
                progress = createProgressFromData(status, correctCount, incorrectCount);
            }

            return vocabularyMapper.toWithProgressResponse(vocabulary, progress, isStarred);
        });

        return createPageResponse(dtoPage, pageable);
    }

    private LearningProgress createProgressFromData(LearningStatus status, int correctCount, int incorrectCount) {

        LearningProgress progress = new LearningProgress();
        progress.setStatus(status);
        progress.setCorrectCount(correctCount);
        progress.setIncorrectCount(incorrectCount);
        // We don't need the full progress object with all fields for the response

        return progress;
    }

    private void validateUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound(messageSource, KEY_ENTITY_USER, userId);
        }
    }

    @Override
    @Transactional
    public VocabularyBulkOperationResponse bulkImportVocabularies(VocabularyBulkImportRequest request) {
        log.info("Bulk importing {} vocabularies to module ID: {}", request.getItems().length, request.getModuleId());

        // Validate and get study module
        final StudyModule module = studyModuleRepository.findById(request.getModuleId()).orElseThrow(() -> {
            log.error(LOG_MODULE_NOT_FOUND, request.getModuleId());
            return KardioException.resourceNotFound(messageSource, KEY_ENTITY_MODULE, request.getModuleId());
        });

        return processBulkImport(request, module);
    }

    private VocabularyBulkOperationResponse processBulkImport(VocabularyBulkImportRequest request, StudyModule module) {

        int successCount = 0;
        int failCount = 0;
        final List<String> errors = new ArrayList<>();
        final List<Vocabulary> vocabulariesToSave = new ArrayList<>();

        // Process each vocabulary item
        for (VocabularyCreateRequest item : request.getItems()) {
            try {
                // Validate required fields
                if (StringUtils.isEmpty(item.getTerm()) || StringUtils.isEmpty(item.getDefinition())) {
                    throw new IllegalArgumentException("Term and definition are required");
                }

                // Create vocabulary entity
                final Vocabulary vocabulary = vocabularyMapper.createFromRequest(item, module);
                vocabulariesToSave.add(vocabulary);
                successCount++;

            } catch (Exception e) {
                log.error("Failed to import vocabulary term '{}': {}", item.getTerm(), e.getMessage());
                failCount++;
                errors.add("Failed to import term '" + item.getTerm() + "': " + e.getMessage());
            }
        }

        // Batch save all successful vocabularies
        if (!vocabulariesToSave.isEmpty()) {
            vocabularyRepository.saveAll(vocabulariesToSave);
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
        validateUser(userId);

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

        if (StringUtils.isEmpty(term) || term.length() < 2) {
            throw KardioException.validationError(messageSource, "error.validation.searchterm", 2);
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
            return KardioException.resourceNotFound(messageSource, KEY_ENTITY_VOCABULARY, id);
        });
    }
}