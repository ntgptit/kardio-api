package com.kardio.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.user.UserDetailedResponse;
import com.kardio.dto.user.UserResponse;
import com.kardio.dto.user.UserUpdateRequest;
import com.kardio.entity.User;
import com.kardio.exception.KardioException;
import com.kardio.mapper.UserMapper;
import com.kardio.repository.UserRepository;
import com.kardio.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of UserService that demonstrates the use of mappers.
 * This is an example of how to use the generic mappers in service
 * implementations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Gets all users with pagination.
     *
     * @param pageable Pagination information
     * @return Page of user responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination: {}", pageable);

        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserResponse> dtoPage = userMapper.toDtoPage(userPage);

        return PageResponse
            .<UserResponse>builder()
            .content(dtoPage.getContent())
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(dtoPage.getTotalElements())
            .totalPages(dtoPage.getTotalPages())
            .first(dtoPage.isFirst())
            .last(dtoPage.isLast())
            .build();
    }

    /**
     * Gets a user by ID.
     *
     * @param id User ID
     * @return User response
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.debug("Getting user by ID: {}", id);

        User user = findUserById(id);
        return userMapper.toDto(user);
    }

    /**
     * Gets detailed user information by ID (admin view).
     *
     * @param id User ID
     * @return Detailed user response
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetailedResponse getUserDetailedById(UUID id) {
        log.debug("Getting detailed user by ID: {}", id);

        User user = findUserById(id);
        return userMapper.toDetailedResponse(user);
    }

    /**
     * Updates a user.
     *
     * @param id      User ID
     * @param request Update request
     * @return Updated user response
     */
    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = findUserById(id);

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    /**
     * Deletes a user (soft delete).
     *
     * @param id User ID
     */
    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);

        User user = findUserById(id);
        user.softDelete();
        userRepository.save(user);
    }

    /**
     * Finds a user by ID or throws an exception.
     *
     * @param id User ID
     * @return User entity
     * @throws KardioException if the user is not found
     */
    private User findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found with ID: {}", id);
            return KardioException.resourceNotFound("User", id);
        });
    }
}