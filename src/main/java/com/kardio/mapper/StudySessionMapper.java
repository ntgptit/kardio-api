package com.kardio.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.kardio.dto.session.SessionItemResponse;
import com.kardio.dto.session.StudySessionDetailedResponse;
import com.kardio.dto.session.StudySessionResponse;
import com.kardio.entity.StudyModule;
import com.kardio.entity.StudySession;
import com.kardio.entity.User;
import com.kardio.entity.enums.SessionType;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for StudySession entity.
 * Handles mapping between StudySession entities and related DTOs.
 */
@Component
@RequiredArgsConstructor
public class StudySessionMapper extends AbstractGenericMapper<StudySession, StudySessionResponse> {

    private final StudyModuleMapper studyModuleMapper;

    @Override
    protected StudySessionResponse mapToDto(StudySession entity) {
        if (entity == null) {
            return null;
        }

        Long durationSeconds = null;
        if (entity.getDuration() != null) {
            durationSeconds = entity.getDuration().getSeconds();
        }

        return StudySessionResponse
            .builder()
            .id(entity.getId())
            .moduleId(entity.getModule().getId())
            .moduleName(entity.getModule().getName())
            .sessionType(entity.getSessionType())
            .startTime(entity.getStartTime())
            .endTime(entity.getEndTime())
            .totalItems(entity.getTotalItems())
            .correctItems(entity.getCorrectItems())
            .accuracyRate(entity.getAccuracyRate())
            .durationSeconds(durationSeconds)
            .build();
    }

    @Override
    protected StudySession mapToEntity(StudySessionResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // User and Module would need to be set separately
        StudySession session = new StudySession();
        session.setSessionType(dto.getSessionType());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setTotalItems(dto.getTotalItems());
        session.setCorrectItems(dto.getCorrectItems());

        return session;
    }

    @Override
    protected StudySession mapDtoToEntity(StudySessionResponse dto, StudySession entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getEndTime() != null) {
            entity.setEndTime(dto.getEndTime());
        }
        if (dto.getTotalItems() != null) {
            entity.setTotalItems(dto.getTotalItems());
        }
        if (dto.getCorrectItems() != null) {
            entity.setCorrectItems(dto.getCorrectItems());
        }
        // Other properties typically not updated

        return entity;
    }

    /**
     * Creates a new StudySession.
     *
     * @param user        The user
     * @param module      The study module
     * @param sessionType The session type
     * @return A new StudySession entity
     */
    public StudySession createSession(User user, StudyModule module, SessionType sessionType) {

        if (user == null || module == null || sessionType == null) {
            return null;
        }

        return StudySession.builder().user(user).module(module).sessionType(sessionType).build();
    }

    /**
     * Maps a StudySession to a detailed response with session items.
     *
     * @param session The study session
     * @param items   List of session item responses
     * @return A detailed study session response
     */
    public StudySessionDetailedResponse toDetailedResponse(StudySession session, List<SessionItemResponse> items) {

        if (session == null) {
            return null;
        }

        Long durationSeconds = null;
        if (session.getDuration() != null) {
            durationSeconds = session.getDuration().getSeconds();
        }

        return StudySessionDetailedResponse
            .builder()
            .id(session.getId())
            .moduleId(session.getModule().getId())
            .moduleName(session.getModule().getName())
            .sessionType(session.getSessionType())
            .startTime(session.getStartTime())
            .endTime(session.getEndTime())
            .totalItems(session.getTotalItems())
            .correctItems(session.getCorrectItems())
            .accuracyRate(session.getAccuracyRate())
            .durationSeconds(durationSeconds)
            .items(items != null ? items : Collections.emptyList())
            .module(studyModuleMapper.toDto(session.getModule()))
            .build();
    }
}