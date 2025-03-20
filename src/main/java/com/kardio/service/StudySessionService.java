package com.kardio.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.session.SessionItemAnalysisResponse;
import com.kardio.dto.session.StudySessionCreateRequest;
import com.kardio.dto.session.StudySessionDetailedResponse;
import com.kardio.dto.session.StudySessionEndRequest;
import com.kardio.dto.session.StudySessionRecordAttemptRequest;
import com.kardio.dto.session.StudySessionResponse;
import com.kardio.entity.enums.SessionType;

/**
 * Service interface for study session management.
 */
public interface StudySessionService {

    /**
     * Creates a new study session.
     *
     * @param request The session creation request
     * @param userId  The ID of the user creating the session
     * @return The created session
     */
    StudySessionResponse createSession(StudySessionCreateRequest request, UUID userId);

    /**
     * Gets a session by ID.
     *
     * @param id     Session ID
     * @param userId User ID for access check
     * @return The session response
     */
    StudySessionResponse getSessionById(UUID id, UUID userId);

    /**
     * Gets detailed session information by ID with items.
     *
     * @param id     Session ID
     * @param userId User ID for access check
     * @return The detailed session response
     */
    StudySessionDetailedResponse getSessionDetailedById(UUID id, UUID userId);

    /**
     * Records an attempt in a session.
     *
     * @param request The record attempt request
     * @param userId  User ID for access check
     * @return The updated session
     */
    StudySessionResponse recordAttempt(StudySessionRecordAttemptRequest request, UUID userId);

    /**
     * Ends a session.
     *
     * @param request The end session request
     * @param userId  User ID for access check
     * @return The ended session
     */
    StudySessionResponse endSession(StudySessionEndRequest request, UUID userId);

    /**
     * Gets sessions by user.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Paginated list of sessions
     */
    PageResponse<StudySessionResponse> getSessionsByUser(UUID userId, Pageable pageable);

    /**
     * Gets sessions by module.
     *
     * @param moduleId Module ID
     * @param userId   User ID for access check
     * @param pageable Pagination information
     * @return Paginated list of sessions
     */
    PageResponse<StudySessionResponse> getSessionsByModule(UUID moduleId, UUID userId, Pageable pageable);

    /**
     * Gets sessions by user and type.
     *
     * @param userId      User ID
     * @param sessionType Session type
     * @param pageable    Pagination information
     * @return Paginated list of sessions
     */
    PageResponse<StudySessionResponse> getSessionsByUserAndType(
            UUID userId,
            SessionType sessionType,
            Pageable pageable);

    /**
     * Analyzes a session.
     *
     * @param id     Session ID
     * @param userId User ID for access check
     * @return Session analysis
     */
    SessionItemAnalysisResponse analyzeSession(UUID id, UUID userId);

    /**
     * Gets active sessions for a user.
     *
     * @param userId User ID
     * @return List of active sessions
     */
    List<StudySessionResponse> getActiveSessionsByUser(UUID userId);

    /**
     * Gets total study time for a user within a date range.
     *
     * @param userId    User ID
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @return Total study time in seconds
     */
    Long getTotalStudyTimeInSeconds(UUID userId, LocalDate startDate, LocalDate endDate);

    /**
     * Deletes a session.
     *
     * @param id     Session ID
     * @param userId User ID for access check
     * @return Success response
     */
    SuccessResponse deleteSession(UUID id, UUID userId);
}