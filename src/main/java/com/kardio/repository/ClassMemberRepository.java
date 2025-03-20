package com.kardio.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kardio.entity.ClassMember;
import com.kardio.entity.enums.MemberRole;

@Repository
public interface ClassMemberRepository extends JpaRepository<ClassMember, UUID> {

    /**
     * Finds members by class ID.
     *
     * @param classId Class ID
     * @return List of class members
     */
    List<ClassMember> findByClassEntityId(UUID classId);

    /**
     * Finds a member by class ID and user ID.
     *
     * @param classId Class ID
     * @param userId  User ID
     * @return Optional containing the member if found
     */
    Optional<ClassMember> findByClassEntityIdAndUserId(UUID classId, UUID userId);

    /**
     * Checks if a member exists by class ID and user ID.
     *
     * @param classId Class ID
     * @param userId  User ID
     * @return true if the member exists, false otherwise
     */
    boolean existsByClassEntityIdAndUserId(UUID classId, UUID userId);

    /**
     * Counts members by class ID.
     *
     * @param classId Class ID
     * @return Count of members
     */
    long countByClassEntityId(UUID classId);

    /**
     * Finds members by class ID and role.
     *
     * @param classId Class ID
     * @param role    Member role
     * @return List of class members with the specified role
     */
    List<ClassMember> findByClassEntityIdAndRole(UUID classId, MemberRole role);

    /**
     * Deletes a member by class ID and user ID.
     *
     * @param classId Class ID
     * @param userId  User ID
     */
    void deleteByClassEntityIdAndUserId(UUID classId, UUID userId);

    /**
     * Finds classes where a user has a specific role.
     *
     * @param userId User ID
     * @param role   Member role
     * @return List of class IDs
     */
    @Query("SELECT m.classEntity.id FROM ClassMember m WHERE m.user.id = :userId AND m.role = :role")
    List<UUID> findClassIdsByUserIdAndRole(@Param("userId") UUID userId, @Param("role") MemberRole role);
}