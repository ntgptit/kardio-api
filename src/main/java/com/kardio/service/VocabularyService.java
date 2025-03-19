package com.kardio.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.vocabulary.VocabularyBulkImportRequest;
import com.kardio.dto.vocabulary.VocabularyBulkOperationResponse;
import com.kardio.dto.vocabulary.VocabularyCreateRequest;
import com.kardio.dto.vocabulary.VocabularyResponse;
import com.kardio.dto.vocabulary.VocabularyUpdateRequest;
import com.kardio.dto.vocabulary.VocabularyWithProgressResponse;

/**
 * Service interface for vocabulary-related operations.
 */
public interface VocabularyService {

    /**
     * Creates a new vocabulary item.
     *
     * @param request The vocabulary creation request
     * @return The created vocabulary
     */
    VocabularyResponse createVocabulary(VocabularyCreateRequest request);

    /**
     * Gets a vocabulary by ID.
     *
     * @param id Vocabulary ID
     * @return The vocabulary response
     */
    VocabularyResponse getVocabularyById(UUID id);

    /**
     * Gets a vocabulary with learning progress.
     *
     * @param id     Vocabulary ID
     * @param userId User ID for progress lookup (optional)
     * @return The vocabulary with progress
     */
    VocabularyWithProgressResponse getVocabularyWithProgress(UUID id, UUID userId);

    /**
     * Updates a vocabulary.
     *
     * @param id      Vocabulary ID
     * @param request The update request
     * @return The updated vocabulary
     */
    VocabularyResponse updateVocabulary(UUID id, VocabularyUpdateRequest request);

    /**
     * Deletes a vocabulary (soft delete).
     *
     * @param id Vocabulary ID
     * @return Success response
     */
    SuccessResponse deleteVocabulary(UUID id);

    /**
     * Gets all vocabularies in a module.
     *
     * @param moduleId Module ID
     * @param pageable Pagination information
     * @return Paginated list of vocabularies
     */
    PageResponse<VocabularyResponse> getVocabulariesByModule(UUID moduleId, Pageable pageable);

    /**
     * Gets all vocabularies in a module with learning progress.
     *
     * @param moduleId Module ID
     * @param userId   User ID for progress lookup
     * @param pageable Pagination information
     * @return Paginated list of vocabularies with progress
     */
    PageResponse<VocabularyWithProgressResponse>
            getVocabulariesByModuleWithProgress(UUID moduleId, UUID userId, Pageable pageable);

    /**
     * Bulk imports vocabularies.
     *
     * @param request The bulk import request
     * @return Operation response with success/failure counts
     */
    VocabularyBulkOperationResponse bulkImportVocabularies(VocabularyBulkImportRequest request);

    /**
     * Gets starred vocabularies for a user.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Paginated list of starred vocabularies
     */
    PageResponse<VocabularyResponse> getStarredVocabularies(UUID userId, Pageable pageable);

    /**
     * Searches vocabularies by term.
     *
     * @param term     Search term
     * @param pageable Pagination information
     * @return Paginated list of matching vocabularies
     */
    PageResponse<VocabularyResponse> searchVocabularies(String term, Pageable pageable);
}