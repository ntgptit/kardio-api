package com.kardio.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.user.UserDetailedResponse;
import com.kardio.dto.user.UserResponse;
import com.kardio.dto.user.UserUpdateRequest;

/**
 * Service interface for user-related operations.
 */
public interface UserService {

    /**
     * Gets all users with pagination.
     *
     * @param pageable Pagination information
     * @return Page of user responses
     */
    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Gets a user by ID.
     *
     * @param id User ID
     * @return User response
     */
    UserResponse getUserById(UUID id);

    /**
     * Gets detailed user information by ID (admin view).
     *
     * @param id User ID
     * @return Detailed user response
     */
    UserDetailedResponse getUserDetailedById(UUID id);

    /**
     * Updates a user.
     *
     * @param id      User ID
     * @param request Update request
     * @return Updated user response
     */
    UserResponse updateUser(UUID id, UserUpdateRequest request);

    /**
     * Deletes a user (soft delete).
     *
     * @param id User ID
     */
    void deleteUser(UUID id);
}