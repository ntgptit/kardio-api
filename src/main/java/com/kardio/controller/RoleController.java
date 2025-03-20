package com.kardio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.role.RoleCreateRequest;
import com.kardio.dto.role.RoleResponse;
import com.kardio.dto.role.RoleUpdateRequest;
import com.kardio.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for role operations.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Endpoints for managing roles")
public class RoleController {

    private final RoleService roleService;

    /**
     * Gets all roles.
     *
     * @return List of all roles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        final List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Gets a role by ID.
     *
     * @param id Role ID
     * @return The role response
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        final RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Creates a new role.
     *
     * @param request The role create request
     * @return The created role
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new role")
    public ResponseEntity<RoleResponse> createRole(
            @Valid
            @RequestBody RoleCreateRequest request) {
        final RoleResponse role = roleService.createRole(request);
        return new ResponseEntity<>(role, HttpStatus.CREATED);
    }

    /**
     * Updates a role.
     *
     * @param id      Role ID
     * @param request The update request
     * @return The updated role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a role")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid
            @RequestBody RoleUpdateRequest request) {
        final RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(role);
    }

    /**
     * Deletes a role.
     *
     * @param id Role ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a role")
    public ResponseEntity<SuccessResponse> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(SuccessResponse.of("Role deleted successfully"));
    }
}