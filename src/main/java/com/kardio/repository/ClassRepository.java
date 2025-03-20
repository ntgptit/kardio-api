package com.kardio.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kardio.entity.Class;

@Repository
public interface ClassRepository extends JpaRepository<Class, UUID> {

    /**
     * Finds classes by creator ID with pagination.
     *
     * @param creatorId Creator ID
     * @param pageable  Pagination information
     * @return Page of classes
     */
    Page<Class> findByCreatorId(UUID creatorId, Pageable pageable);

    /**
     * Finds all active classes for a user (where the user is a member).
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Page of classes
     */
    @Query("SELECT c FROM Class c JOIN ClassMember m ON c.id = m.classEntity.id "
            + "WHERE m.user.id = :userId AND c.deletedAt IS NULL")
    Page<Class> findByMemberId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Searches classes by name (case insensitive).
     *
     * @param term     Search term
     * @param pageable Pagination information
     * @return Page of matching classes
     */
    Page<Class> findByNameContainingIgnoreCase(String term, Pageable pageable);

    /**
     * Counts class members.
     *
     * @param classId Class ID
     * @return Count of members
     */
    @Query("SELECT COUNT(m) FROM ClassMember m WHERE m.classEntity.id = :classId")
    long countMembersByClassId(@Param("classId") UUID classId);

    /**
     * Counts class modules.
     *
     * @param classId Class ID
     * @return Count of modules
     */
    @Query("SELECT COUNT(m) FROM ClassModule m WHERE m.classEntity.id = :classId")
    long countModulesByClassId(@Param("classId") UUID classId);

    /**
     * Efficiently count members for multiple classes at once.
     *
     * @param classIds Collection of class IDs
     * @return List of arrays containing [classId, memberCount]
     */
    @Query("SELECT m.classEntity.id as classId, COUNT(m) as count " + "FROM ClassMember m "
            + "WHERE m.classEntity.id IN :classIds " + "GROUP BY m.classEntity.id")
    List<Object[]> countMembersForClasses(@Param("classIds") List<UUID> classIds);

    /**
     * Efficiently count modules for multiple classes at once.
     *
     * @param classIds Collection of class IDs
     * @return List of arrays containing [classId, moduleCount]
     */
    @Query("SELECT m.classEntity.id as classId, COUNT(m) as count " + "FROM ClassModule m "
            + "WHERE m.classEntity.id IN :classIds " + "GROUP BY m.classEntity.id")
    List<Object[]> countModulesForClasses(@Param("classIds") List<UUID> classIds);
}