package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.role.RoleCreateRequest;
import com.kardio.dto.role.RoleResponse;
import com.kardio.dto.role.RoleUpdateRequest;
import com.kardio.entity.Role;

/**
 * Mapper for Role entity.
 */
@Component
public class RoleMapper extends AbstractGenericMapper<Role, RoleResponse> {

    @Override
    protected RoleResponse mapToDto(Role entity) {
        if (entity == null) {
            return null;
        }

        return RoleResponse
            .builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .build();
    }

    @Override
    protected Role mapToEntity(RoleResponse dto) {
        if (dto == null) {
            return null;
        }

        return Role.builder().name(dto.getName()).description(dto.getDescription()).build();
    }

    @Override
    protected Role mapDtoToEntity(RoleResponse dto, Role entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        // Name typically not updated for security reasons

        return entity;
    }

    /**
     * Creates a new Role entity from a RoleCreateRequest.
     *
     * @param request The create request
     * @return A new Role entity
     */
    public Role createFromRequest(RoleCreateRequest request) {
        if (request == null) {
            return null;
        }

        return Role.builder().name(request.getName()).description(request.getDescription()).build();
    }

    /**
     * Updates a Role entity from a RoleUpdateRequest.
     *
     * @param request The update request
     * @param role    The role to update
     * @return The updated Role
     */
    public Role updateFromRequest(RoleUpdateRequest request, Role role) {
        if (request == null || role == null) {
            return role;
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        return role;
    }
}