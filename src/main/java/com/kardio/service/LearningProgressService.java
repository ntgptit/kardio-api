package com.kardio.service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service interface for learning progress management.
 */
public interface LearningProgressService {

    /**
     * Records an attempt for a vocabulary.
     *
     * @param userId       User ID
     * @param vocabularyId Vocabulary ID
     * @param isCorrect    Whether the attempt was correct
     * @return true if the attempt was recorded successfully
     */
    boolean recordAttempt(UUID userId, UUID vocabularyId, boolean isCorrect);

    /**
     * Updates the next review time for a vocabulary.
     *
     * @param userId       User ID
     * @param vocabularyId Vocabulary ID
     * @param nextReviewAt Next review time
     * @return true if the update was successful
     */
    boolean updateNextReviewTime(UUID userId, UUID vocabularyId, LocalDateTime nextReviewAt);
}