package com.kardio.controller;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.vocabulary.VocabularyBulkImportRequest;
import com.kardio.dto.vocabulary.VocabularyBulkOperationResponse;
import com.kardio.dto.vocabulary.VocabularyCreateRequest;
import com.kardio.dto.vocabulary.VocabularyResponse;
import com.kardio.dto.vocabulary.VocabularyUpdateRequest;
import com.kardio.dto.vocabulary.VocabularyWithProgressResponse;
import com.kardio.security.CustomUserDetails;
import com.kardio.service.VocabularyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for vocabulary operations.
 */
@RestController
@RequestMapping("/api/v1/vocabularies")
@RequiredArgsConstructor
@Tag(name = "Vocabulary Management", description = "Endpoints for managing vocabularies")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    /**
     * Creates a new vocabulary item.
     *
     * @param request The vocabulary creation request
     * @return The created vocabulary
     */
    @PostMapping
    @Operation(summary = "Create a new vocabulary item")
    public ResponseEntity<VocabularyResponse> createVocabulary(
            @Valid
            @RequestBody VocabularyCreateRequest request) {
        VocabularyResponse vocabulary = vocabularyService.createVocabulary(request);
        return new ResponseEntity<>(vocabulary, HttpStatus.CREATED);
    }

    /**
     * Gets a vocabulary by ID.
     *
     * @param id Vocabulary ID
     * @return The vocabulary response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get vocabulary by ID")
    public ResponseEntity<VocabularyResponse> getVocabularyById(@PathVariable UUID id) {
        VocabularyResponse vocabulary = vocabularyService.getVocabularyById(id);
        return ResponseEntity.ok(vocabulary);
    }

    /**
     * Gets a vocabulary with learning progress.
     *
     * @param id          Vocabulary ID
     * @param userDetails Authenticated user details
     * @return The vocabulary with progress
     */
    @GetMapping("/{id}/progress")
    @Operation(summary = "Get vocabulary with learning progress")
    public
            ResponseEntity<VocabularyWithProgressResponse>
            getVocabularyWithProgress(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        VocabularyWithProgressResponse vocabulary = vocabularyService.getVocabularyWithProgress(id, userId);
        return ResponseEntity.ok(vocabulary);
    }

    /**
     * Updates a vocabulary.
     *
     * @param id      Vocabulary ID
     * @param request The update request
     * @return The updated vocabulary
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a vocabulary")
    public ResponseEntity<VocabularyResponse> updateVocabulary(
            @PathVariable UUID id,
            @Valid
            @RequestBody VocabularyUpdateRequest request) {

        VocabularyResponse vocabulary = vocabularyService.updateVocabulary(id, request);
        return ResponseEntity.ok(vocabulary);
    }

    /**
     * Deletes a vocabulary (soft delete).
     *
     * @param id Vocabulary ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vocabulary (soft delete)")
    public ResponseEntity<SuccessResponse> deleteVocabulary(@PathVariable UUID id) {
        SuccessResponse response = vocabularyService.deleteVocabulary(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all vocabularies in a module.
     *
     * @param moduleId  Module ID
     * @param page      Page number (0-based)
     * @param size      Page size
     * @param sort      Sort field
     * @param direction Sort direction
     * @return Paginated list of vocabularies
     */
    @GetMapping("/module/{moduleId}")
    @Operation(summary = "Get all vocabularies in a module")
    public ResponseEntity<PageResponse<VocabularyResponse>> getVocabulariesByModule(
            @PathVariable UUID moduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<VocabularyResponse> response = vocabularyService.getVocabulariesByModule(moduleId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all vocabularies in a module with learning progress.
     *
     * @param moduleId    Module ID
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of vocabularies with progress
     */
    @GetMapping("/module/{moduleId}/progress")
    @Operation(summary = "Get all vocabularies in a module with learning progress")
    public ResponseEntity<PageResponse<VocabularyWithProgressResponse>> getVocabulariesByModuleWithProgress(
            @PathVariable UUID moduleId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        UUID userId = userDetails.getUser().getId();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<VocabularyWithProgressResponse> response = vocabularyService
            .getVocabulariesByModuleWithProgress(moduleId, userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Bulk imports vocabularies.
     *
     * @param request The bulk import request
     * @return Operation response with success/failure counts
     */
    @PostMapping("/bulk-import")
    @Operation(summary = "Bulk import vocabularies")
    public ResponseEntity<VocabularyBulkOperationResponse> bulkImportVocabularies(
            @Valid
            @RequestBody VocabularyBulkImportRequest request) {

        VocabularyBulkOperationResponse response = vocabularyService.bulkImportVocabularies(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets starred vocabularies for the current user.
     *
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of starred vocabularies
     */
    @GetMapping("/starred")
    @Operation(summary = "Get starred vocabularies for the current user")
    public ResponseEntity<PageResponse<VocabularyResponse>> getStarredVocabularies(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        UUID userId = userDetails.getUser().getId();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<VocabularyResponse> response = vocabularyService.getStarredVocabularies(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches vocabularies by term.
     *
     * @param term      Search term
     * @param page      Page number (0-based)
     * @param size      Page size
     * @param sort      Sort field
     * @param direction Sort direction
     * @return Paginated list of matching vocabularies
     */
    @GetMapping("/search")
    @Operation(summary = "Search vocabularies by term")
    public ResponseEntity<PageResponse<VocabularyResponse>> searchVocabularies(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "term") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<VocabularyResponse> response = vocabularyService.searchVocabularies(term, pageable);
        return ResponseEntity.ok(response);
    }
}