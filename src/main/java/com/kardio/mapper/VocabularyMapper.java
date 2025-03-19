package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.vocabulary.VocabularyCreateRequest;
import com.kardio.dto.vocabulary.VocabularyResponse;
import com.kardio.dto.vocabulary.VocabularyUpdateRequest;
import com.kardio.dto.vocabulary.VocabularyWithProgressResponse;
import com.kardio.entity.LearningProgress;
import com.kardio.entity.StudyModule;
import com.kardio.entity.Vocabulary;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for Vocabulary entity.
 * Handles mapping between Vocabulary entities and various related DTOs.
 */
@Component
@RequiredArgsConstructor
public class VocabularyMapper extends AbstractGenericMapper<Vocabulary, VocabularyResponse> {

    private final LearningProgressMapper progressMapper;

    @Override
    protected VocabularyResponse mapToDto(Vocabulary entity) {
        if (entity == null) {
            return null;
        }

        return VocabularyResponse
            .builder()
            .id(entity.getId())
            .moduleId(entity.getModule().getId())
            .term(entity.getTerm())
            .definition(entity.getDefinition())
            .example(entity.getExample())
            .pronunciation(entity.getPronunciation())
            .partOfSpeech(entity.getPartOfSpeech())
            .difficultyLevel(entity.getDifficultyLevel())
            .createdAt(entity.getCreatedAt())
            // isStarred will need to be set separately based on user context
            .isStarred(false)
            .build();
    }

    @Override
    protected Vocabulary mapToEntity(VocabularyResponse dto) {
        if (dto == null) {
            return null;
        }

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setTerm(dto.getTerm());
        vocabulary.setDefinition(dto.getDefinition());
        vocabulary.setExample(dto.getExample());
        vocabulary.setPronunciation(dto.getPronunciation());
        vocabulary.setPartOfSpeech(dto.getPartOfSpeech());
        vocabulary.setDifficultyLevel(dto.getDifficultyLevel());
        // Module needs to be set separately

        return vocabulary;
    }

    @Override
    protected Vocabulary mapDtoToEntity(VocabularyResponse dto, Vocabulary entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getTerm() != null) {
            entity.setTerm(dto.getTerm());
        }
        if (dto.getDefinition() != null) {
            entity.setDefinition(dto.getDefinition());
        }
        if (dto.getExample() != null) {
            entity.setExample(dto.getExample());
        }
        if (dto.getPronunciation() != null) {
            entity.setPronunciation(dto.getPronunciation());
        }
        if (dto.getPartOfSpeech() != null) {
            entity.setPartOfSpeech(dto.getPartOfSpeech());
        }
        if (dto.getDifficultyLevel() != null) {
            entity.setDifficultyLevel(dto.getDifficultyLevel());
        }

        return entity;
    }

    /**
     * Creates a new Vocabulary entity from a VocabularyCreateRequest.
     *
     * @param request The request containing vocabulary data
     * @param module  The StudyModule to associate with the vocabulary
     * @return A new Vocabulary entity
     */
    public Vocabulary createFromRequest(VocabularyCreateRequest request, StudyModule module) {
        if (request == null) {
            return null;
        }

        return Vocabulary
            .builder()
            .module(module)
            .term(request.getTerm())
            .definition(request.getDefinition())
            .example(request.getExample())
            .pronunciation(request.getPronunciation())
            .partOfSpeech(request.getPartOfSpeech())
            .difficultyLevel(request.getDifficultyLevel())
            .build();
    }

    /**
     * Updates a Vocabulary entity from a VocabularyUpdateRequest.
     *
     * @param request    The request with update data
     * @param vocabulary The vocabulary to update
     * @return The updated Vocabulary
     */
    public Vocabulary updateFromRequest(VocabularyUpdateRequest request, Vocabulary vocabulary) {
        if (request == null || vocabulary == null) {
            return vocabulary;
        }

        if (request.getTerm() != null) {
            vocabulary.setTerm(request.getTerm());
        }
        if (request.getDefinition() != null) {
            vocabulary.setDefinition(request.getDefinition());
        }
        if (request.getExample() != null) {
            vocabulary.setExample(request.getExample());
        }
        if (request.getPronunciation() != null) {
            vocabulary.setPronunciation(request.getPronunciation());
        }
        if (request.getPartOfSpeech() != null) {
            vocabulary.setPartOfSpeech(request.getPartOfSpeech());
        }
        if (request.getDifficultyLevel() != null) {
            vocabulary.setDifficultyLevel(request.getDifficultyLevel());
        }

        return vocabulary;
    }

    /**
     * Maps a Vocabulary and LearningProgress to a VocabularyWithProgressResponse.
     *
     * @param vocabulary The vocabulary entity
     * @param progress   The learning progress entity
     * @param isStarred  Whether the vocabulary is starred by the user
     * @return A VocabularyWithProgressResponse
     */
    public
            VocabularyWithProgressResponse
            toWithProgressResponse(Vocabulary vocabulary, LearningProgress progress, boolean isStarred) {

        if (vocabulary == null) {
            return null;
        }

        VocabularyWithProgressResponse response = VocabularyWithProgressResponse
            .builder()
            .id(vocabulary.getId())
            .moduleId(vocabulary.getModule().getId())
            .term(vocabulary.getTerm())
            .definition(vocabulary.getDefinition())
            .example(vocabulary.getExample())
            .pronunciation(vocabulary.getPronunciation())
            .partOfSpeech(vocabulary.getPartOfSpeech())
            .difficultyLevel(vocabulary.getDifficultyLevel())
            .createdAt(vocabulary.getCreatedAt())
            .isStarred(isStarred)
            .build();

        if (progress != null) {
            response.setProgress(progressMapper.toDto(progress));
        }

        return response;
    }
}