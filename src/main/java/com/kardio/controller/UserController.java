package com.kardio.controller;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.user.UserDetailedResponse;
import com.kardio.dto.user.UserResponse;
import com.kardio.dto.user.UserUpdateRequest;
import com.kardio.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for user operations that demonstrates how controllers work with
 * mappers.
 * This controller directly uses DTOs from the service layer, which are mapped
 * by the
 * generic mappers in the service implementation.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {

    private final UserService userService;

    /**
     * Gets all users with pagination.
     *
     * @param page      Page number (0-based)
     * @param size      Page size
     * @param sort      Sort field
     * @param direction Sort direction
     * @return Page of user responses
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a user by ID.
     *
     * @param id User ID
     * @return User response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Gets detailed user information by ID (admin view).
     *
     * @param id User ID
     * @return Detailed user response
     */
    @GetMapping("/{id}/detailed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get detailed user information by ID (admin view)")
    public ResponseEntity<UserDetailedResponse> getUserDetailedById(@PathVariable UUID id) {
        UserDetailedResponse user = userService.getUserDetailedById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Updates a user.
     *
     * @param id      User ID
     * @param request Update request
     * @return Updated user response
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid
            @RequestBody UserUpdateRequest request) {

        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user (soft delete).
     *
     * @param id User ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user (soft delete)")
    public ResponseEntity<SuccessResponse> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(SuccessResponse.of("User deleted successfully"), HttpStatus.OK);
    }
}