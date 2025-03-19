package com.kardio.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kardio.entity.StudyModule;
import com.kardio.entity.enums.VisibilityType;

/**
 * Repository for StudyModule entity.
 */
@Repository
public interface StudyModuleRepository extends JpaRepository<StudyModule, UUID> {

    /**
     * Finds modules by creator ID.
     *
     * @param creatorId Creator ID
     * @param pageable  Pagination information
     * @return Page of study modules
     */
    Page<StudyModule> findByCreatorId(UUID creatorId, Pageable pageable);

    /**
     * Finds modules by folder ID.
     *
     * @param folderId Folder ID
     * @param pageable Pagination information
     * @return Page of study modules
     */
    Page<StudyModule> findByFolderId(UUID folderId, Pageable pageable);

    /**
     * Finds modules by creator ID and visibility.
     *
     * @param creatorId  Creator ID
     * @param visibility Visibility type
     * @param pageable   Pagination information
     * @return Page of study modules
     */
    Page<StudyModule> findByCreatorIdAndVisibility(UUID creatorId, VisibilityType visibility, Pageable pageable);

    /**
     * Finds public modules.
     *
     * @param pageable Pagination information
     * @return Page of public study modules
     */
    Page<StudyModule> findByVisibility(VisibilityType visibility, Pageable pageable);

    /**
     * Finds modules by name containing the given string (case insensitive).
     *
     * @param name     Name to search for
     * @param pageable Pagination information
     * @return Page of study modules
     */
    Page<StudyModule> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Searches modules by name or description (case insensitive).
     *
     * @param term     Search term
     * @param pageable Pagination information
     * @return Page of study modules
     */
    @Query("SELECT m FROM StudyModule m WHERE " + "LOWER(m.name) LIKE LOWER(CONCAT('%', :term, '%')) OR "
            + "LOWER(m.description) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<StudyModule> searchByNameOrDescription(@Param("term") String term, Pageable pageable);

    /**
     * Searches public modules by name or description (case insensitive).
     *
     * @param term       Search term
     * @param visibility Visibility type
     * @param pageable   Pagination information
     * @return Page of study modules
     */
    @Query("SELECT m FROM StudyModule m WHERE " + "(LOWER(m.name) LIKE LOWER(CONCAT('%', :term, '%')) OR "
            + "LOWER(m.description) LIKE LOWER(CONCAT('%', :term, '%'))) AND " + "m.visibility = :visibility")
    Page<StudyModule> searchPublicByNameOrDescription(
            @Param("term") String term,
            @Param("visibility") VisibilityType visibility,
            Pageable pageable);

    /**
     * Finds modules shared with a user.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Page of shared study modules
     */
    @Query("SELECT m FROM StudyModule m JOIN SharedStudyModule s ON m.id = s.studyModule.id "
            + "WHERE s.user.id = :userId AND m.deletedAt IS NULL")
    Page<StudyModule> findSharedWithUser(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Counts modules by creator ID.
     *
     * @param creatorId Creator ID
     * @return Count of study modules
     */
    long countByCreatorId(UUID creatorId);

    /**
     * Counts modules by folder ID.
     *
     * @param folderId Folder ID
     * @return Count of study modules
     */
    long countByFolderId(UUID folderId);

    /**
     * Finds modules by creator ID and folder ID.
     *
     * @param creatorId Creator ID
     * @param folderId  Folder ID
     * @param pageable  Pagination information
     * @return Page of study modules
     */
    Page<StudyModule> findByCreatorIdAndFolderId(UUID creatorId, UUID folderId, Pageable pageable);

    /**
     * Finds modules accessible to a user (created by them, public, or shared with
     * them).
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Page of accessible study modules
     */
    @Query("SELECT DISTINCT m FROM StudyModule m LEFT JOIN SharedStudyModule s ON m.id = s.studyModule.id "
            + "WHERE m.creator.id = :userId OR m.visibility = 'PUBLIC' OR s.user.id = :userId")
    Page<StudyModule> findAccessibleToUser(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Checks if a module is accessible to a user (created by them, public, or
     * shared with them).
     *
     * @param moduleId Module ID
     * @param userId   User ID
     * @return true if the module is accessible, false otherwise
     */
    @Query("SELECT COUNT(m) > 0 FROM StudyModule m LEFT JOIN SharedStudyModule s ON m.id = s.studyModule.id "
            + "WHERE m.id = :moduleId AND (m.creator.id = :userId OR m.visibility = 'PUBLIC' OR s.user.id = :userId)")
    boolean isAccessibleToUser(@Param("moduleId") UUID moduleId, @Param("userId") UUID userId);

    /**
     * Gets recent modules for a user.
     *
     * @param userId User ID
     * @param limit  Maximum number of modules to return
     * @return List of recent study modules
     */
    @Query("SELECT m FROM StudyModule m " + "WHERE m.creator.id = :userId OR m.visibility = 'PUBLIC' "
            + "ORDER BY m.lastStudiedAt DESC NULLS LAST")
    List<StudyModule> findRecentModules(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds modules by IDs that are accessible to a user.
     *
     * @param moduleIds Module IDs
     * @param userId    User ID
     * @return List of accessible study modules
     */
    @Query("SELECT m FROM StudyModule m LEFT JOIN SharedStudyModule s ON m.id = s.studyModule.id "
            + "WHERE m.id IN :moduleIds AND (m.creator.id = :userId OR m.visibility = 'PUBLIC' OR s.user.id = :userId)")
    List<StudyModule> findByIdsAccessibleToUser(@Param("moduleIds") List<UUID> moduleIds, @Param("userId") UUID userId);
}