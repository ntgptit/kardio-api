package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.session.SessionItemResponse;
import com.kardio.dto.session.StudySessionRecordAttemptRequest;
import com.kardio.entity.SessionItem;
import com.kardio.entity.StudySession;
import com.kardio.entity.Vocabulary;

/**
 * Mapper for SessionItem entity.
 * Handles mapping between SessionItem entities and related DTOs.
 */
@Component
public class SessionItemMapper extends AbstractGenericMapper<SessionItem, SessionItemResponse> {

    @Override
    protected SessionItemResponse mapToDto(SessionItem entity) {
        if (entity == null) {
            return null;
        }

        Double responseTimeSeconds = null;
        if (entity.getResponseTimeMs() != null) {
            responseTimeSeconds = entity.getResponseTimeInSeconds();
        }

        return SessionItemResponse
            .builder()
            .id(entity.getId())
            .vocabularyId(entity.getVocabulary().getId())
            .term(entity.getVocabulary().getTerm())
            .definition(entity.getVocabulary().getDefinition())
            .isCorrect(entity.getIsCorrect())
            .responseTimeMs(entity.getResponseTimeMs())
            .responseTimeSeconds(responseTimeSeconds)
            .createdAt(entity.getCreatedAt())
            .build();
    }

    @Override
    protected SessionItem mapToEntity(SessionItemResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // Session and Vocabulary would need to be set separately
        SessionItem item = new SessionItem();
        item.setIsCorrect(dto.getIsCorrect());
        item.setResponseTimeMs(dto.getResponseTimeMs());

        return item;
    }

    @Override
    protected SessionItem mapDtoToEntity(SessionItemResponse dto, SessionItem entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getIsCorrect() != null) {
            entity.setIsCorrect(dto.getIsCorrect());
        }
        if (dto.getResponseTimeMs() != null) {
            entity.setResponseTimeMs(dto.getResponseTimeMs());
        }
        // Other properties typically not updated

        return entity;
    }

    /**
     * Creates a new SessionItem from a record attempt request.
     *
     * @param session    The study session
     * @param vocabulary The vocabulary being studied
     * @param request    The record attempt request
     * @return A new SessionItem entity
     */
    public
            SessionItem
            createFromRequest(StudySession session, Vocabulary vocabulary, StudySessionRecordAttemptRequest request) {

        if (session == null || vocabulary == null || request == null) {
            return null;
        }

        return SessionItem
            .builder()
            .session(session)
            .vocabulary(vocabulary)
            .isCorrect(request.getIsCorrect())
            .responseTimeMs(request.getResponseTimeMs())
            .build();
    }
}