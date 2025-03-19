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
import com.kardio.dto.folder.FolderCreateRequest;
import com.kardio.dto.folder.FolderDetailedResponse;
import com.kardio.dto.folder.FolderHierarchyResponse;
import com.kardio.dto.folder.FolderMoveRequest;
import com.kardio.dto.folder.FolderResponse;
import com.kardio.dto.folder.FolderUpdateRequest;
import com.kardio.security.CustomUserDetails;
import com.kardio.service.FolderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for folder operations.
 */
@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
@Tag(name = "Folder Management", description = "Endpoints for managing folders")
public class FolderController {

    private final FolderService folderService;

    /**
     * Creates a new folder.
     *
     * @param request     The folder creation request
     * @param userDetails Authenticated user details
     * @return The created folder
     */
    @PostMapping
    @Operation(summary = "Create a new folder")
    public ResponseEntity<FolderResponse> createFolder(
            @Valid
            @RequestBody FolderCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        FolderResponse folder = folderService.createFolder(request, userId);
        return new ResponseEntity<>(folder, HttpStatus.CREATED);
    }

    /**
     * Gets a folder by ID.
     *
     * @param id          Folder ID
     * @param userDetails Authenticated user details
     * @return The folder response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get folder by ID")
    public
            ResponseEntity<FolderResponse>
            getFolderById(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        FolderResponse folder = folderService.getFolderById(id, userId);
        return ResponseEntity.ok(folder);
    }

    /**
     * Gets detailed folder information by ID.
     *
     * @param id          Folder ID
     * @param userDetails Authenticated user details
     * @return The detailed folder response
     */
    @GetMapping("/{id}/detailed")
    @Operation(summary = "Get detailed folder information by ID")
    public
            ResponseEntity<FolderDetailedResponse>
            getFolderDetailedById(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        FolderDetailedResponse folder = folderService.getFolderDetailedById(id, userId);
        return ResponseEntity.ok(folder);
    }

    /**
     * Updates a folder.
     *
     * @param id          Folder ID
     * @param request     The update request
     * @param userDetails Authenticated user details
     * @return The updated folder
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a folder")
    public ResponseEntity<FolderResponse> updateFolder(
            @PathVariable UUID id,
            @Valid
            @RequestBody FolderUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        FolderResponse folder = folderService.updateFolder(id, request, userId);
        return ResponseEntity.ok(folder);
    }

    /**
     * Deletes a folder (soft delete).
     *
     * @param id          Folder ID
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a folder (soft delete)")
    public
            ResponseEntity<SuccessResponse>
            deleteFolder(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        SuccessResponse response = folderService.deleteFolder(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all root folders for the current user.
     *
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of root folders
     */
    @GetMapping("/root")
    @Operation(summary = "Get all root folders for the current user")
    public ResponseEntity<PageResponse<FolderResponse>> getRootFolders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        UUID userId = userDetails.getUser().getId();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<FolderResponse> response = folderService.getRootFolders(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all subfolders for a folder.
     *
     * @param parentId    Parent folder ID
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of subfolders
     */
    @GetMapping("/parent/{parentId}")
    @Operation(summary = "Get all subfolders for a folder")
    public ResponseEntity<PageResponse<FolderResponse>> getSubfolders(
            @PathVariable UUID parentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        UUID userId = userDetails.getUser().getId();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<FolderResponse> response = folderService.getSubfolders(parentId, userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets the complete folder hierarchy for the current user.
     *
     * @param userDetails Authenticated user details
     * @return The folder hierarchy
     */
    @GetMapping("/hierarchy")
    @Operation(summary = "Get the complete folder hierarchy for the current user")
    public ResponseEntity<List<FolderHierarchyResponse>> getFolderHierarchy(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        List<FolderHierarchyResponse> hierarchy = folderService.getFolderHierarchy(userId);
        return ResponseEntity.ok(hierarchy);
    }

    /**
     * Moves a folder to another parent folder.
     *
     * @param id          Folder ID
     * @param request     The move request
     * @param userDetails Authenticated user details
     * @return The moved folder
     */
    @PutMapping("/{id}/move")
    @Operation(summary = "Move a folder to another parent folder")
    public ResponseEntity<FolderResponse> moveFolder(
            @PathVariable UUID id,
            @Valid
            @RequestBody FolderMoveRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        FolderResponse folder = folderService.moveFolder(id, request, userId);
        return ResponseEntity.ok(folder);
    }
}