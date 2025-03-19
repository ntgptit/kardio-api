package com.kardio.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kardio.entity.Folder;

/**
 * Repository for Folder entity.
 */
@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    /**
     * Finds folders by user ID.
     *
     * @param userId User ID
     * @return List of folders
     */
    List<Folder> findAllByUserId(UUID userId);

    /**
     * Finds root folders (no parent) by user ID with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Page of folders
     */
    Page<Folder> findByUserIdAndParentFolderIsNull(UUID userId, Pageable pageable);

    /**
     * Finds subfolders by parent folder ID.
     *
     * @param parentFolderId Parent folder ID
     * @return List of subfolders
     */
    List<Folder> findByParentFolderId(UUID parentFolderId);

    /**
     * Finds subfolders by parent folder ID with pagination.
     *
     * @param parentFolderId Parent folder ID
     * @param pageable       Pagination information
     * @return Page of subfolders
     */
    Page<Folder> findByParentFolderId(UUID parentFolderId, Pageable pageable);

    /**
     * Counts modules per folder for a user.
     * This is a native query that returns [folder_id, module_count] pairs.
     *
     * @param userId User ID
     * @return List of folder ID and module count pairs
     */
    @Query(
        value = "SELECT f.id AS folder_id, COUNT(m.id) AS module_count " + "FROM folders f "
                + "LEFT JOIN study_modules m ON f.id = m.folder_id AND m.deleted_at IS NULL "
                + "WHERE f.user_id = :userId AND f.deleted_at IS NULL " + "GROUP BY f.id",
        nativeQuery = true)
    List<Object[]> countModulesPerFolder(@Param("userId") UUID userId);

    /**
     * Checks if a folder contains modules.
     *
     * @param folderId Folder ID
     * @return true if the folder contains modules, false otherwise
     */
    @Query("SELECT COUNT(m) > 0 FROM StudyModule m WHERE m.folder.id = :folderId AND m.deletedAt IS NULL")
    boolean hasModules(@Param("folderId") UUID folderId);

    /**
     * Checks if a folder has subfolders.
     *
     * @param folderId Folder ID
     * @return true if the folder has subfolders, false otherwise
     */
    @Query("SELECT COUNT(f) > 0 FROM Folder f WHERE f.parentFolder.id = :folderId AND f.deletedAt IS NULL")
    boolean hasSubfolders(@Param("folderId") UUID folderId);
}