package com.kardio.controller;

import java.util.List;
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
import com.kardio.dto.module.StudyModuleCreateRequest;
import com.kardio.dto.module.StudyModuleDetailedResponse;
import com.kardio.dto.module.StudyModuleResponse;
import com.kardio.dto.module.StudyModuleShareRequest;
import com.kardio.dto.module.StudyModuleSummaryResponse;
import com.kardio.dto.module.StudyModuleUpdateRequest;
import com.kardio.security.CustomUserDetails;
import com.kardio.service.StudyModuleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for study module operations.
 */
@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
@Tag(name = "Study Module Management", description = "Endpoints for managing study modules")
public class StudyModuleController {

    private final StudyModuleService studyModuleService;

    /**
     * Creates a new study module.
     *
     * @param request     The module creation request
     * @param userDetails Authenticated user details
     * @return The created module
     */
    @PostMapping
    @Operation(summary = "Create a new study module")
    public ResponseEntity<StudyModuleResponse> createModule(
            @Valid
            @RequestBody StudyModuleCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        StudyModuleResponse module = studyModuleService.createModule(request, userId);
        return new ResponseEntity<>(module, HttpStatus.CREATED);
    }

    /**
     * Gets a module by ID.
     *
     * @param id          Module ID
     * @param userDetails Authenticated user details
     * @return The module response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get module by ID")
    public
            ResponseEntity<StudyModuleResponse>
            getModuleById(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        StudyModuleResponse module = studyModuleService.getModuleById(id, userId);
        return ResponseEntity.ok(module);
    }

    /**
     * Gets detailed module information by ID.
     *
     * @param id          Module ID
     * @param userDetails Authenticated user details
     * @return The detailed module response
     */
    @GetMapping("/{id}/detailed")
    @Operation(summary = "Get detailed module information by ID")
    public
            ResponseEntity<StudyModuleDetailedResponse>
            getModuleDetailedById(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        StudyModuleDetailedResponse module = studyModuleService.getModuleDetailedById(id, userId);
        return ResponseEntity.ok(module);
    }

    /**
     * Updates a module.
     *
     * @param id          Module ID
     * @param request     The update request
     * @param userDetails Authenticated user details
     * @return The updated module
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a module")
    public ResponseEntity<StudyModuleResponse> updateModule(
            @PathVariable UUID id,
            @Valid
            @RequestBody StudyModuleUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        StudyModuleResponse module = studyModuleService.updateModule(id, request, userId);
        return ResponseEntity.ok(module);
    }

    /**
     * Deletes a module (soft delete).
     *
     * @param id          Module ID
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a module (soft delete)")
    public
            ResponseEntity<SuccessResponse>
            deleteModule(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        SuccessResponse response = studyModuleService.deleteModule(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all modules created by the current user.
     *
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of modules
     */
    @GetMapping("/my")
    @Operation(summary = "Get all modules created by the current user")
    public ResponseEntity<PageResponse<StudyModuleSummaryResponse>> getMyModules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        UUID userId = userDetails.getUser().getId();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<StudyModuleSummaryResponse> response = studyModuleService.getModulesByUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all modules in a folder.
     *
     * @param folderId    Folder ID
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of modules
     */
    @GetMapping("/folder/{folderId}")
    @Operation(summary = "Get all modules in a folder")
    public ResponseEntity<PageResponse<StudyModuleSummaryResponse>> getModulesByFolder(
            @PathVariable UUID folderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        UUID userId = userDetails.getUser().getId();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<StudyModuleSummaryResponse> response = studyModuleService
            .getModulesByFolder(folderId, userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all public modules.
     *
     * @param page      Page number (0-based)
     * @param size      Page size
     * @param sort      Sort field
     * @param direction Sort direction
     * @return Paginated list of public modules
     */
    @GetMapping("/public")
    @Operation(summary = "Get all public modules")
    public ResponseEntity<PageResponse<StudyModuleSummaryResponse>> getPublicModules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<StudyModuleSummaryResponse> response = studyModuleService.getPublicModules(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets modules shared with the current user.
     *
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of shared modules
     */
    @GetMapping("/shared")
    @Operation(summary = "Get modules shared with the current user")
    public ResponseEntity<PageResponse<StudyModuleSummaryResponse>> getSharedModules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        UUID userId = userDetails.getUser().getId();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<StudyModuleSummaryResponse> response = studyModuleService.getSharedModules(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches modules by term.
     *
     * @param term        Search term
     * @param publicOnly  Whether to search only public modules
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of matching modules
     */
    @GetMapping("/search")
    @Operation(summary = "Search modules by term")
    public ResponseEntity<PageResponse<StudyModuleSummaryResponse>> searchModules(
            @RequestParam String term,
            @RequestParam(defaultValue = "false") boolean publicOnly,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        UUID userId = userDetails.getUser().getId();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<StudyModuleSummaryResponse> response = studyModuleService
            .searchModules(term, publicOnly, userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Shares a module with users.
     *
     * @param id          Module ID
     * @param request     The share request
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @PostMapping("/{id}/share")
    @Operation(summary = "Share a module with users")
    public ResponseEntity<SuccessResponse> shareModule(
            @PathVariable UUID id,
            @Valid
            @RequestBody StudyModuleShareRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        SuccessResponse response = studyModuleService.shareModule(id, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Unshares a module with a user.
     *
     * @param id          Module ID
     * @param userId      User ID to unshare with
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @DeleteMapping("/{id}/share/{userId}")
    @Operation(summary = "Unshare a module with a user")
    public ResponseEntity<SuccessResponse> unshareModule(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID ownerId = userDetails.getUser().getId();
        SuccessResponse response = studyModuleService.unshareModule(id, userId, ownerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets recent modules for the current user.
     *
     * @param userDetails Authenticated user details
     * @param limit       Maximum number of modules to return
     * @return List of recent modules
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent modules for the current user")
    public ResponseEntity<List<StudyModuleSummaryResponse>> getRecentModules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit) {

        UUID userId = userDetails.getUser().getId();
        List<StudyModuleSummaryResponse> modules = studyModuleService.getRecentModules(userId, limit);
        return ResponseEntity.ok(modules);
    }
}