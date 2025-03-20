package com.kardio.service;

import java.util.List;

import com.kardio.dto.role.RoleCreateRequest;
import com.kardio.dto.role.RoleResponse;
import com.kardio.dto.role.RoleUpdateRequest;
import com.kardio.entity.enums.RoleType;

/**
 * Service interface for role management.
 */
public interface RoleService {

    /**
     * Gets a role by ID.
     *
     * @param id Role ID
     * @return The role response
     */
    RoleResponse getRoleById(Long id);

    /**
     * Gets a role by name.
     *
     * @param name Role name
     * @return The role response
     */
    RoleResponse getRoleByName(String name);

    /**
     * Gets a role by type.
     *
     * @param roleType Role type
     * @return The role response
     */
    RoleResponse getRoleByType(RoleType roleType);

    /**
     * Gets all roles.
     *
     * @return List of all roles
     */
    List<RoleResponse> getAllRoles();

    /**
     * Creates a new role.
     *
     * @param request The role create request
     * @return The created role
     */
    RoleResponse createRole(RoleCreateRequest request);

    /**
     * Updates a role.
     *
     * @param id      Role ID
     * @param request The update request
     * @return The updated role
     */
    RoleResponse updateRole(Long id, RoleUpdateRequest request);

    /**
     * Deletes a role.
     *
     * @param id Role ID
     */
    void deleteRole(Long id);
}