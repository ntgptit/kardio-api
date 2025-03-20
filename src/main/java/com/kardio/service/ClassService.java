package com.kardio.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.kardio.dto.classroom.ClassCreateRequest;
import com.kardio.dto.classroom.ClassDetailedResponse;
import com.kardio.dto.classroom.ClassResponse;
import com.kardio.dto.classroom.ClassUpdateRequest;
import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.entity.enums.MemberRole;

/**
 * Service interface for class management.
 */
public interface ClassService {

    /**
     * Creates a new class.
     *
     * @param request   Class creation request
     * @param creatorId The ID of the user creating the class
     * @return The created class
     */
    ClassResponse createClass(ClassCreateRequest request, UUID creatorId);

    /**
     * Gets a class by ID.
     *
     * @param id     Class ID
     * @param userId User ID for access check
     * @return The class response
     */
    ClassResponse getClassById(UUID id, UUID userId);

    /**
     * Gets detailed class information by ID with members and modules.
     *
     * @param id     Class ID
     * @param userId User ID for access check
     * @return The detailed class response
     */
    ClassDetailedResponse getClassDetailedById(UUID id, UUID userId);

    /**
     * Updates a class.
     *
     * @param id      Class ID
     * @param request The update request
     * @param userId  User ID for ownership check
     * @return The updated class
     */
    ClassResponse updateClass(UUID id, ClassUpdateRequest request, UUID userId);

    /**
     * Deletes a class (soft delete).
     *
     * @param id     Class ID
     * @param userId User ID for ownership check
     * @return Success response
     */
    SuccessResponse deleteClass(UUID id, UUID userId);

    /**
     * Gets all classes created by a user.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Paginated list of classes
     */
    PageResponse<ClassResponse> getClassesByCreator(UUID userId, Pageable pageable);

    /**
     * Gets all classes where a user is a member.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Paginated list of classes
     */
    PageResponse<ClassResponse> getClassesByMember(UUID userId, Pageable pageable);

    /**
     * Searches classes by name.
     *
     * @param term     Search term
     * @param pageable Pagination information
     * @return Paginated list of matching classes
     */
    PageResponse<ClassResponse> searchClasses(String term, Pageable pageable);

    /**
     * Gets the role of a user in a class.
     *
     * @param classId Class ID
     * @param userId  User ID
     * @return The user's role or null if not a member
     */
    MemberRole getUserRole(UUID classId, UUID userId);

    /**
     * Checks if a user can modify a class.
     *
     * @param classId Class ID
     * @param userId  User ID
     * @return true if the user can modify the class
     */
    boolean canModifyClass(UUID classId, UUID userId);
}