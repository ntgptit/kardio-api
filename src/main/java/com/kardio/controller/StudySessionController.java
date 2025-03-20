package com.kardio.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.session.SessionItemAnalysisResponse;
import com.kardio.dto.session.StudySessionCreateRequest;
import com.kardio.dto.session.StudySessionDetailedResponse;
import com.kardio.dto.session.StudySessionEndRequest;
import com.kardio.dto.session.StudySessionRecordAttemptRequest;
import com.kardio.dto.session.StudySessionResponse;
import com.kardio.entity.enums.SessionType;
import com.kardio.security.CustomUserDetails;
import com.kardio.service.StudySessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for study session operations.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Study Session Management", description = "Endpoints for managing study sessions")
public class StudySessionController {

    private final StudySessionService studySessionService;

    /**
     * Creates a new study session.
     *
     * @param request     The session creation request
     * @param userDetails Authenticated user details
     * @return The created session
     */
    @PostMapping
    @Operation(summary = "Create a new study session")
    public ResponseEntity<StudySessionResponse> createSession(
            @Valid
            @RequestBody StudySessionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final StudySessionResponse response = studySessionService.createSession(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Gets a session by ID.
     *
     * @param id          Session ID
     * @param userDetails Authenticated user details
     * @return The session response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID")
    public ResponseEntity<StudySessionResponse> getSessionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final StudySessionResponse response = studySessionService.getSessionById(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets detailed session information by ID with items.
     *
     * @param id          Session ID
     * @param userDetails Authenticated user details
     * @return The detailed session response
     */
    @GetMapping("/{id}/detailed")
    @Operation(summary = "Get detailed session information by ID with items")
    public ResponseEntity<StudySessionDetailedResponse> getSessionDetailedById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final StudySessionDetailedResponse response = studySessionService.getSessionDetailedById(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Records an attempt in a session.
     *
     * @param request     The record attempt request
     * @param userDetails Authenticated user details
     * @return The updated session
     */
    @PostMapping("/record-attempt")
    @Operation(summary = "Record an attempt in a session")
    public ResponseEntity<StudySessionResponse> recordAttempt(
            @Valid
            @RequestBody StudySessionRecordAttemptRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final StudySessionResponse response = studySessionService.recordAttempt(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Ends a session.
     *
     * @param request     The end session request
     * @param userDetails Authenticated user details
     * @return The ended session
     */
    @PostMapping("/end")
    @Operation(summary = "End a session")
    public ResponseEntity<StudySessionResponse> endSession(
            @Valid
            @RequestBody StudySessionEndRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final StudySessionResponse response = studySessionService.endSession(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets sessions by user.
     *
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of sessions
     */
    @GetMapping("/my")
    @Operation(summary = "Get all sessions for the current user")
    public ResponseEntity<PageResponse<StudySessionResponse>> getMyStudySessions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        final UUID userId = userDetails.getUser().getId();
        final Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        final Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        final PageResponse<StudySessionResponse> response = studySessionService.getSessionsByUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets sessions by module.
     *
     * @param moduleId    Module ID
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of sessions
     */
    @GetMapping("/module/{moduleId}")
    @Operation(summary = "Get all sessions for a module")
    public ResponseEntity<PageResponse<StudySessionResponse>> getSessionsByModule(
            @PathVariable UUID moduleId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        final UUID userId = userDetails.getUser().getId();
        final Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        final Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        final PageResponse<StudySessionResponse> response = studySessionService
            .getSessionsByModule(moduleId, userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets sessions by user and type.
     *
     * @param sessionType Session type
     * @param userDetails Authenticated user details
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sort        Sort field
     * @param direction   Sort direction
     * @return Paginated list of sessions
     */
    @GetMapping("/type/{sessionType}")
    @Operation(summary = "Get all sessions for the current user by type")
    public ResponseEntity<PageResponse<StudySessionResponse>> getSessionsByType(
            @PathVariable SessionType sessionType,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        final UUID userId = userDetails.getUser().getId();
        final Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        final Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        final PageResponse<StudySessionResponse> response = studySessionService
            .getSessionsByUserAndType(userId, sessionType, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes a session.
     *
     * @param id          Session ID
     * @param userDetails Authenticated user details
     * @return Session analysis
     */
    @GetMapping("/{id}/analyze")
    @Operation(summary = "Analyze a session")
    public ResponseEntity<SessionItemAnalysisResponse> analyzeSession(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final SessionItemAnalysisResponse response = studySessionService.analyzeSession(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets active sessions for the current user.
     *
     * @param userDetails Authenticated user details
     * @return List of active sessions
     */
    @GetMapping("/active")
    @Operation(summary = "Get active sessions for the current user")
    public ResponseEntity<List<StudySessionResponse>> getActiveStudySessions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final List<StudySessionResponse> response = studySessionService.getActiveSessionsByUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets total study time for the current user within a date range.
     *
     * @param startDate   Start date (inclusive)
     * @param endDate     End date (inclusive)
     * @param userDetails Authenticated user details
     * @return Total study time in seconds
     */
    @GetMapping("/total-time")
    @Operation(summary = "Get total study time for the current user within a date range")
    public ResponseEntity<Long> getTotalStudyTime(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final Long totalTimeSeconds = studySessionService.getTotalStudyTimeInSeconds(userId, startDate, endDate);
        return ResponseEntity.ok(totalTimeSeconds);
    }

    /**
     * Deletes a session.
     *
     * @param id          Session ID
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a session")
    public ResponseEntity<SuccessResponse> deleteSession(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        final UUID userId = userDetails.getUser().getId();
        final SuccessResponse response = studySessionService.deleteSession(id, userId);
        return ResponseEntity.ok(response);
    }
}