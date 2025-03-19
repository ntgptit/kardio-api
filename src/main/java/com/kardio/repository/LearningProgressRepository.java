package com.kardio.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kardio.entity.LearningProgress;
import com.kardio.entity.enums.LearningStatus;

/**
 * Repository for LearningProgress entity.
 */
@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, UUID> {

    /**
     * Finds learning progress by vocabulary ID and user ID.
     *
     * @param vocabularyId Vocabulary ID
     * @param userId       User ID
     * @return Optional containing the learning progress if found
     */
    Optional<LearningProgress> findByVocabularyIdAndUserId(UUID vocabularyId, UUID userId);

    /**
     * Finds learning progress by user ID and vocabulary IDs.
     *
     * @param userId        User ID
     * @param vocabularyIds List of vocabulary IDs
     * @return List of learning progress entries
     */
    List<LearningProgress> findByUserIdAndVocabularyIdIn(UUID userId, List<UUID> vocabularyIds);

    /**
     * Counts learning progress by user ID, module ID, and status.
     *
     * @param userId   User ID
     * @param moduleId Module ID
     * @param status   Learning status
     * @return Count of learning progress entries
     */
    long countByUserIdAndVocabulary_Module_IdAndStatus(UUID userId, UUID moduleId, LearningStatus status);

    /**
     * Checks if learning progress exists by vocabulary ID and user ID.
     *
     * @param vocabularyId Vocabulary ID
     * @param userId       User ID
     * @return true if progress exists, false otherwise
     */
    boolean existsByVocabularyIdAndUserId(UUID vocabularyId, UUID userId);
}