package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.progress.LearningProgressResponse;
import com.kardio.entity.LearningProgress;
import com.kardio.entity.User;
import com.kardio.entity.Vocabulary;
import com.kardio.entity.enums.LearningStatus;

/**
 * Mapper for LearningProgress entity.
 * Handles mapping between LearningProgress entities and related DTOs.
 */
@Component
public class LearningProgressMapper extends AbstractGenericMapper<LearningProgress, LearningProgressResponse> {

    @Override
    protected LearningProgressResponse mapToDto(LearningProgress entity) {
        if (entity == null) {
            return null;
        }

        return LearningProgressResponse
            .builder()
            .id(entity.getId())
            .vocabularyId(entity.getVocabulary().getId())
            .status(entity.getStatus())
            .correctCount(entity.getCorrectCount())
            .incorrectCount(entity.getIncorrectCount())
            .accuracyRate(entity.getAccuracyRate())
            .lastStudiedAt(entity.getLastStudiedAt())
            .nextReviewAt(entity.getNextReviewAt())
            .build();
    }

    @Override
    protected LearningProgress mapToEntity(LearningProgressResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // In real usage, User and Vocabulary would need to be set separately
        LearningProgress progress = new LearningProgress();
        progress.setStatus(dto.getStatus());
        progress.setCorrectCount(dto.getCorrectCount());
        progress.setIncorrectCount(dto.getIncorrectCount());
        progress.setLastStudiedAt(dto.getLastStudiedAt());
        progress.setNextReviewAt(dto.getNextReviewAt());

        return progress;
    }

    @Override
    protected LearningProgress mapDtoToEntity(LearningProgressResponse dto, LearningProgress entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getCorrectCount() != null) {
            entity.setCorrectCount(dto.getCorrectCount());
        }
        if (dto.getIncorrectCount() != null) {
            entity.setIncorrectCount(dto.getIncorrectCount());
        }
        if (dto.getLastStudiedAt() != null) {
            entity.setLastStudiedAt(dto.getLastStudiedAt());
        }
        if (dto.getNextReviewAt() != null) {
            entity.setNextReviewAt(dto.getNextReviewAt());
        }

        return entity;
    }

    /**
     * Creates a new LearningProgress entity for a user and vocabulary.
     *
     * @param user       The user
     * @param vocabulary The vocabulary
     * @return A new LearningProgress entity
     */
    public LearningProgress createInitialProgress(User user, Vocabulary vocabulary) {
        if (user == null || vocabulary == null) {
            return null;
        }

        return LearningProgress
            .builder()
            .user(user)
            .vocabulary(vocabulary)
            .status(LearningStatus.NOT_STUDIED)
            .correctCount(0)
            .incorrectCount(0)
            .build();
    }
}