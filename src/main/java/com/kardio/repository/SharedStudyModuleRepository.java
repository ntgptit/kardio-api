package com.kardio.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kardio.entity.SharedStudyModule;

/**
 * Repository for SharedStudyModule entity.
 */
@Repository
public interface SharedStudyModuleRepository extends JpaRepository<SharedStudyModule, UUID> {

    /**
     * Checks if a module is shared with a user.
     *
     * @param studyModuleId Study module ID
     * @param userId        User ID
     * @return true if the module is shared with the user, false otherwise
     */
    boolean existsByStudyModuleIdAndUserId(UUID studyModuleId, UUID userId);

    /**
     * Counts shares for a module.
     *
     * @param studyModuleId Study module ID
     * @return Count of shares
     */
    long countByStudyModuleId(UUID studyModuleId);

    /**
     * Deletes a share by module ID and user ID.
     *
     * @param studyModuleId Study module ID
     * @param userId        User ID
     */
    void deleteByStudyModuleIdAndUserId(UUID studyModuleId, UUID userId);
}