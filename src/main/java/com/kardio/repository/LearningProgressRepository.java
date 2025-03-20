package com.kardio.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Gets learning progress statistics for a module in one efficient query.
     * Returns map with masteredCount, learningCount, and averageAccuracy.
     *
     * @param moduleId Module ID
     * @param userId   User ID
     * @return Map with statistics
     */
    @Query("SELECT " + "COUNT(CASE WHEN lp.status = 'MASTERED' THEN 1 ELSE NULL END) as masteredCount, "
            + "COUNT(CASE WHEN lp.status = 'LEARNING' THEN 1 ELSE NULL END) as learningCount, "
            + "AVG(lp.correctCount * 100.0 / (lp.correctCount + lp.incorrectCount)) as averageAccuracy "
            + "FROM LearningProgress lp " + "JOIN lp.vocabulary v "
            + "WHERE v.module.id = :moduleId AND lp.user.id = :userId AND "
            + "(lp.correctCount + lp.incorrectCount) > 0")
    Map<String, Object> getModuleStatistics(@Param("moduleId") UUID moduleId, @Param("userId") UUID userId);

    /**
     * Gets batch learning progress status for multiple vocabulary items.
     *
     * @param userId        User ID
     * @param vocabularyIds List of vocabulary IDs
     * @return Map of vocabulary ID to learning status
     */
    @Query("SELECT v.id as vocabularyId, lp.status as status " + "FROM Vocabulary v LEFT JOIN LearningProgress lp "
            + "ON v.id = lp.vocabulary.id AND lp.user.id = :userId " + "WHERE v.id IN :vocabularyIds")
    List<Object[]> getBatchLearningStatus(
            @Param("userId") UUID userId,
            @Param("vocabularyIds") List<UUID> vocabularyIds);
}