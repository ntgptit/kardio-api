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

import com.kardio.dto.classroom.ClassCreateRequest;
import com.kardio.dto.classroom.ClassDetailedResponse;
import com.kardio.dto.classroom.ClassResponse;
import com.kardio.dto.classroom.ClassUpdateRequest;
import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.security.CustomUserDetails;
import com.kardio.service.ClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for class operations.
 */
@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Class Management", description = "Endpoints for managing classes")
public class ClassController {

    private final ClassService classService;

    /**
     * Creates a new class.
     *
     * @param request     The class creation request
     * @param userDetails Authenticated user details
     * @return The created class
     */
    @PostMapping
    @Operation(summary = "Create a new class")
    public ResponseEntity<ClassResponse> createClass(
            @Valid
            @RequestBody ClassCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final ClassResponse response = classService.createClass(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Gets a class by ID.
     *
     * @param id          Class ID
     * @param userDetails Authenticated user details
     * @return The class response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID")
    public ResponseEntity<ClassResponse> getClassById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final ClassResponse response = classService.getClassById(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets detailed class information by ID.
     *
     * @param id          Class ID
     * @param userDetails Authenticated user details
     * @return The detailed class response
     */
    @GetMapping("/{id}/detailed")
    @Operation(summary = "Get detailed class information by ID")
    public ResponseEntity<ClassDetailedResponse> getClassDetailedById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final ClassDetailedResponse response = classService.getClassDetailedById(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a class.
     *
     * @param id          Class ID
     * @param request     The update request
     * @param userDetails Authenticated user details
     * @return The updated class
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a class")
    public ResponseEntity<ClassResponse> updateClass(
            @PathVariable UUID id,
            @Valid
            @RequestBody ClassUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final ClassResponse response = classService.updateClass(id, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a class (soft delete).
     *
     * @param id          Class ID
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a class (soft delete)")
    public ResponseEntity<SuccessResponse> deleteClass(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final SuccessResponse response = classService.deleteClass(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all classes created by the current user.
     *
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of classes
     */
    @GetMapping("/my")
    @Operation(summary = "Get all classes created by the current user")
    public ResponseEntity<PageResponse<ClassResponse>> getMyClasses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        final UUID userId = userDetails.getUser().getId();
        final Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        final Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        final PageResponse<ClassResponse> response = classService.getClassesByCreator(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all classes where the current user is a member.
     *
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of classes
     */
    @GetMapping("/enrolled")
    @Operation(summary = "Get all classes where the current user is a member")
    public ResponseEntity<PageResponse<ClassResponse>> getEnrolledClasses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        final UUID userId = userDetails.getUser().getId();
        final Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        final Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        final PageResponse<ClassResponse> response = classService.getClassesByMember(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches classes by name.
     *
     * @param term      Search term
     * @param page      Page number (0-based)
     * @param size      Page size
     * @param sort      Sort field
     * @param direction Sort direction
     * @return Paginated list of matching classes
     */
    @GetMapping("/search")
    @Operation(summary = "Search classes by name")
    public ResponseEntity<PageResponse<ClassResponse>> searchClasses(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        final Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        final Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        final PageResponse<ClassResponse> response = classService.searchClasses(term, pageable);
        return ResponseEntity.ok(response);
    }
}