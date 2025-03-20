package com.kardio.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.entity.LearningProgress;
import com.kardio.entity.User;
import com.kardio.entity.Vocabulary;
import com.kardio.exception.KardioException;
import com.kardio.repository.LearningProgressRepository;
import com.kardio.repository.UserRepository;
import com.kardio.repository.VocabularyRepository;
import com.kardio.service.LearningProgressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of LearningProgressService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningProgressServiceImpl implements LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;
// private final MessageSource messageSource;

    @Override
    @Transactional
    public boolean recordAttempt(UUID userId, UUID vocabularyId, boolean isCorrect) {
        log
            .debug(
                "Recording attempt for user ID: {}, vocabulary ID: {}, isCorrect: {}",
                userId,
                vocabularyId,
                isCorrect);

        try {
            // Get or create learning progress entity
            LearningProgress progress = getOrCreateProgress(userId, vocabularyId);

            // Calculate next review time based on spaced repetition algorithm
            LocalDateTime nextReviewTime = calculateNextReviewTime(progress, isCorrect);

            // Record attempt
            progress.recordAttempt(isCorrect, nextReviewTime);

            // Save progress
            learningProgressRepository.save(progress);

            log.debug("Attempt recorded successfully");
            return true;
        } catch (Exception e) {
            log.error("Failed to record attempt: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateNextReviewTime(UUID userId, UUID vocabularyId, LocalDateTime nextReviewAt) {
        log
            .debug(
                "Updating next review time for user ID: {}, vocabulary ID: {}, next review at: {}",
                userId,
                vocabularyId,
                nextReviewAt);

        try {
            // Get or create learning progress entity
            LearningProgress progress = getOrCreateProgress(userId, vocabularyId);

            // Update next review time
            progress.setNextReviewAt(nextReviewAt);

            // Save progress
            learningProgressRepository.save(progress);

            log.debug("Next review time updated successfully");
            return true;
        } catch (Exception e) {
            log.error("Failed to update next review time: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets an existing learning progress or creates a new one.
     *
     * @param userId       User ID
     * @param vocabularyId Vocabulary ID
     * @return Learning progress entity
     */
    private LearningProgress getOrCreateProgress(UUID userId, UUID vocabularyId) {
        // Try to find existing progress
        Optional<LearningProgress> progressOpt = learningProgressRepository
            .findByVocabularyIdAndUserId(vocabularyId, userId);

        if (progressOpt.isPresent()) {
            return progressOpt.get();
        }

        // Create new progress if not exists
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new KardioException("User not found", org.springframework.http.HttpStatus.NOT_FOUND));

        Vocabulary vocabulary = vocabularyRepository
            .findById(vocabularyId)
            .orElseThrow(
                () -> new KardioException("Vocabulary not found", org.springframework.http.HttpStatus.NOT_FOUND));

        LearningProgress newProgress = LearningProgress.builder().user(user).vocabulary(vocabulary).build();

        return learningProgressRepository.save(newProgress);
    }

    /**
     * Calculates the next review time based on spaced repetition.
     * Simplified implementation of a spaced repetition algorithm.
     *
     * @param progress  Current learning progress
     * @param isCorrect Whether the latest attempt was correct
     * @return Next review time
     */
    private LocalDateTime calculateNextReviewTime(LearningProgress progress, boolean isCorrect) {
        LocalDateTime now = LocalDateTime.now();

        if (!isCorrect) {
            // If incorrect, review again sooner (30 minutes to 1 hour)
            return now.plusMinutes(30 + (int) (Math.random() * 30));
        }

        // Calculate interval based on correct attempts
        int correctCount = progress.getCorrectCount() != null ? progress.getCorrectCount() : 0;

        // Simple exponential backoff
        int hoursToAdd = switch (correctCount) {
        case 0, 1 -> 4;    // 4 hours
        case 2 -> 8;       // 8 hours
        case 3 -> 24;      // 1 day
        case 4 -> 72;      // 3 days
        case 5 -> 168;     // 1 week
        default -> 336;    // 2 weeks
        };

        return now.plusHours(hoursToAdd);
    }
}