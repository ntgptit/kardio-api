package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.user.UserDetailedResponse;
import com.kardio.dto.user.UserResponse;
import com.kardio.dto.user.UserUpdateRequest;
import com.kardio.entity.Role;
import com.kardio.entity.User;

/**
 * Enhanced mapper for User entity that extends AbstractGenericMapper.
 * Provides mapping between User entity and UserResponse DTO.
 */
@Component
public class UserMapper extends AbstractGenericMapper<User, UserResponse> {

    /**
     * Maps User entity to UserResponse DTO with all user information.
     *
     * @param entity User entity to map
     * @return Mapped UserResponse
     */
    @Override
    protected UserResponse mapToDto(User entity) {
        if (entity == null) {
            return null;
        }

        return UserResponse
            .builder()
            .id(entity.getId())
            .email(entity.getEmail())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .displayName(entity.getDisplayName())
            .createdAt(entity.getCreatedAt())
            .roles(entity.getRoles().stream().map(Role::getName).toList())
            .build();
    }

    /**
     * Maps UserResponse DTO to User entity.
     * Note: This method isn't typically used for security reasons,
     * as users are usually created through registration.
     *
     * @param dto UserResponse DTO to map
     * @return Mapped User entity
     */
    @Override
    protected User mapToEntity(UserResponse dto) {
        if (dto == null) {
            return null;
        }

        // This is a simplified implementation
        // In a real app, you'd need to handle roles properly
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDisplayName(dto.getDisplayName());
        // Note: password should not be set from DTO for security
        return user;
    }

    /**
     * Updates User entity from UserResponse DTO.
     * Only updates fields that are present in the DTO.
     *
     * @param dto    Source UserResponse with updated values
     * @param entity Target User entity to update
     * @return Updated User entity
     */
    @Override
    protected User mapDtoToEntity(UserResponse dto, User entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getDisplayName() != null) {
            entity.setDisplayName(dto.getDisplayName());
        }
        if (dto.getFirstName() != null) {
            entity.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            entity.setLastName(dto.getLastName());
        }
        // Email and roles typically not updated this way for security

        return entity;
    }

    /**
     * Maps User entity to UserDetailedResponse DTO with admin-level information.
     *
     * @param user User entity to map
     * @return Mapped UserDetailedResponse with detailed information
     */
    public UserDetailedResponse toDetailedResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserDetailedResponse
            .builder()
            .id(user.getId())
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .deletedAt(user.getDeletedAt())
            .build();
    }

    /**
     * Updates User entity from UserUpdateRequest DTO.
     * Only updates fields that are present in the request.
     *
     * @param request UserUpdateRequest with update information
     * @param user    User entity to update
     * @return Updated User entity
     */
    public User updateFromRequest(UserUpdateRequest request, User user) {
        if (request == null || user == null) {
            return user;
        }

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        // Password should be handled separately with encryption

        return user;
    }
}