package com.kardio.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.constant.AppConstants.EntityNames;
import com.kardio.constant.AppConstants.ErrorMessages;
import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.session.SessionItemAnalysisResponse;
import com.kardio.dto.session.StudySessionCreateRequest;
import com.kardio.dto.session.StudySessionDetailedResponse;
import com.kardio.dto.session.StudySessionEndRequest;
import com.kardio.dto.session.StudySessionRecordAttemptRequest;
import com.kardio.dto.session.StudySessionResponse;
import com.kardio.entity.SessionItem;
import com.kardio.entity.StudyModule;
import com.kardio.entity.StudySession;
import com.kardio.entity.User;
import com.kardio.entity.Vocabulary;
import com.kardio.entity.enums.SessionType;
import com.kardio.exception.KardioException;
import com.kardio.mapper.SessionItemMapper;
import com.kardio.mapper.StudySessionMapper;
import com.kardio.repository.SessionItemRepository;
import com.kardio.repository.StreakRepository;
import com.kardio.repository.StudyModuleRepository;
import com.kardio.repository.StudySessionRepository;
import com.kardio.repository.UserRepository;
import com.kardio.repository.VocabularyRepository;
import com.kardio.service.LearningProgressService;
import com.kardio.service.StudySessionService;
import com.kardio.util.PageUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of StudySessionService for study session management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudySessionServiceImpl implements StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final SessionItemRepository sessionItemRepository;
    private final StudyModuleRepository studyModuleRepository;
    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;
    private final StreakRepository streakRepository;
    private final StudySessionMapper studySessionMapper;
    private final SessionItemMapper sessionItemMapper;
// private final StudyModuleMapper studyModuleMapper;
    private final LearningProgressService learningProgressService;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public StudySessionResponse createSession(StudySessionCreateRequest request, UUID userId) {
        Objects.requireNonNull(request, "Session creation request cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");

        log.info("Creating study session for user ID: {} with module ID: {}", userId, request.getModuleId());

        // Validate and get user
        final User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found with ID: {}", userId);
            return KardioException.resourceNotFound(messageSource, EntityNames.USER, userId);
        });

        // Validate and get module
        final StudyModule module = studyModuleRepository.findById(request.getModuleId()).orElseThrow(() -> {
            log.error("Study module not found with ID: {}", request.getModuleId());
            return KardioException.resourceNotFound(messageSource, EntityNames.MODULE, request.getModuleId());
        });

        // Check if module is accessible to user
        if (!studyModuleRepository.isAccessibleToUser(request.getModuleId(), userId)) {
            log.error("User {} does not have access to module {}", userId, request.getModuleId());
            throw KardioException
                .forbidden(
                    messageSource,
                    ErrorMessages.FORBIDDEN_RESOURCE,
                    messageSource.getMessage(EntityNames.MODULE, null, LocaleContextHolder.getLocale()));
        }

        // Create session entity
        final StudySession session = studySessionMapper.createSession(user, module, request.getSessionType());
        final StudySession savedSession = studySessionRepository.save(session);

        // Record streak
        updateUserStreak(user);

        // Update module last studied time
        updateModuleLastStudiedTime(module);

        log.info("Session created successfully with ID: {}", savedSession.getId());
        return studySessionMapper.toDto(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public StudySessionResponse getSessionById(UUID id, UUID userId) {
        log.debug("Getting session by ID: {} for user ID: {}", id, userId);

        final StudySession session = findSessionByIdAndValidateOwnership(id, userId);
        return studySessionMapper.toDto(session);
    }

    @Override
    @Transactional(readOnly = true)
    public StudySessionDetailedResponse getSessionDetailedById(UUID id, UUID userId) {
        log.debug("Getting detailed session by ID: {} for user ID: {}", id, userId);

        final StudySession session = findSessionByIdAndValidateOwnership(id, userId);

        // Get session items
        final List<SessionItem> items = sessionItemRepository.findBySessionId(id);

        return studySessionMapper.toDetailedResponse(session, sessionItemMapper.toDtoList(items));
    }

    @Override
    @Transactional
    public StudySessionResponse recordAttempt(StudySessionRecordAttemptRequest request, UUID userId) {
        Objects.requireNonNull(request, "Record attempt request cannot be null");
        Objects.requireNonNull(request.getSessionId(), "Session ID cannot be null");
        Objects.requireNonNull(request.getVocabularyId(), "Vocabulary ID cannot be null");

        log
            .info(
                "Recording attempt for session ID: {} and vocabulary ID: {}",
                request.getSessionId(),
                request.getVocabularyId());

        final StudySession session = findSessionByIdAndValidateOwnership(request.getSessionId(), userId);

        // Check if session is active
        if (session.getEndTime() != null) {
            log.error("Cannot record attempt for ended session: {}", request.getSessionId());
            throw KardioException.validationError(messageSource, "error.session.ended");
        }

        // Validate and get vocabulary
        final Vocabulary vocabulary = vocabularyRepository.findById(request.getVocabularyId()).orElseThrow(() -> {
            log.error("Vocabulary not found with ID: {}", request.getVocabularyId());
            return KardioException.resourceNotFound(messageSource, EntityNames.VOCABULARY, request.getVocabularyId());
        });

        // Check if vocabulary belongs to session's module
        if (!vocabulary.getModule().getId().equals(session.getModule().getId())) {
            log
                .error(
                    "Vocabulary {} does not belong to session's module {}",
                    request.getVocabularyId(),
                    session.getModule().getId());
            throw KardioException.validationError(messageSource, "error.vocabulary.notinmodule");
        }

        // Record attempt in session
        final boolean isCorrect = Boolean.TRUE.equals(request.getIsCorrect());
        session.recordAttempt(isCorrect);

        // Create session item
        final SessionItem item = sessionItemMapper.createFromRequest(session, vocabulary, request);
        sessionItemRepository.save(item);

        // Update learning progress
        learningProgressService.recordAttempt(userId, vocabulary.getId(), isCorrect);

        // Save updated session
        final StudySession updatedSession = studySessionRepository.save(session);

        log.info("Attempt recorded successfully for session: {}", session.getId());
        return studySessionMapper.toDto(updatedSession);
    }

    @Override
    @Transactional
    public StudySessionResponse endSession(StudySessionEndRequest request, UUID userId) {
        Objects.requireNonNull(request, "End session request cannot be null");
        Objects.requireNonNull(request.getSessionId(), "Session ID cannot be null");

        log.info("Ending session with ID: {}", request.getSessionId());

        final StudySession session = findSessionByIdAndValidateOwnership(request.getSessionId(), userId);

        // Check if session is already ended
        if (session.getEndTime() != null) {
            log.warn("Session is already ended: {}", request.getSessionId());
            return studySessionMapper.toDto(session);
        }

        // End session
        session.endSession();
        final StudySession savedSession = studySessionRepository.save(session);

        log.info("Session ended successfully: {}", savedSession.getId());
        return studySessionMapper.toDto(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudySessionResponse> getSessionsByUser(UUID userId, Pageable pageable) {
        log.debug("Getting sessions by user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound(messageSource, EntityNames.USER, userId);
        }

        final Page<StudySession> sessionPage = studySessionRepository.findByUserId(userId, pageable);
        final Page<StudySessionResponse> dtoPage = sessionPage.map(studySessionMapper::toDto);

        return PageUtils.createPageResponse(dtoPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudySessionResponse> getSessionsByModule(UUID moduleId, UUID userId, Pageable pageable) {
        log.debug("Getting sessions by module ID: {} for user ID: {}", moduleId, userId);

        // Validate module exists and is accessible to user
        if (!studyModuleRepository.existsById(moduleId)) {
            log.error("Study module not found with ID: {}", moduleId);
            throw KardioException.resourceNotFound(messageSource, EntityNames.MODULE, moduleId);
        }

        if (!studyModuleRepository.isAccessibleToUser(moduleId, userId)) {
            log.error("User {} does not have access to module {}", userId, moduleId);
            throw KardioException
                .forbidden(
                    messageSource,
                    ErrorMessages.FORBIDDEN_RESOURCE,
                    messageSource.getMessage(EntityNames.MODULE, null, LocaleContextHolder.getLocale()));
        }

        final Page<StudySession> sessionPage = studySessionRepository
            .findByUserIdAndModuleId(userId, moduleId, pageable);

        return PageUtils.createPageResponse(sessionPage, studySessionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudySessionResponse> getSessionsByUserAndType(
            UUID userId,
            SessionType sessionType,
            Pageable pageable) {
        log.debug("Getting sessions by user ID: {} and type: {}", userId, sessionType);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound(messageSource, EntityNames.USER, userId);
        }

        final Page<StudySession> sessionPage = studySessionRepository
            .findByUserIdAndSessionType(userId, sessionType, pageable);

        // Sử dụng PageUtils với mapper function
        return PageUtils.createPageResponse(sessionPage, studySessionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionItemAnalysisResponse analyzeSession(UUID id, UUID userId) {
        log.debug("Analyzing session with ID: {} for user ID: {}", id, userId);

// final StudySession session = findSessionByIdAndValidateOwnership(id, userId);
        final List<SessionItem> items = sessionItemRepository.findBySessionId(id);

        if (items.isEmpty()) {
            log.warn("No items found for session: {}", id);
            return SessionItemAnalysisResponse
                .builder()
                .totalItems(0)
                .correctItems(0)
                .accuracyRate(0.0)
                .averageResponseTimeMs(0.0)
                .difficultTerms(new ArrayList<>())
                .masteredTerms(new ArrayList<>())
                .build();
        }

        // Calculate statistics
        final int totalItems = items.size();
        final int correctItems = (int) items.stream().filter(item -> Boolean.TRUE.equals(item.getIsCorrect())).count();

        final double accuracyRate = totalItems > 0 ? (double) correctItems / totalItems * 100 : 0.0;

        final double averageResponseTimeMs = items
            .stream()
            .filter(item -> item.getResponseTimeMs() != null)
            .mapToInt(SessionItem::getResponseTimeMs)
            .average()
            .orElse(0.0);

        // Group items by vocabulary
        final Map<UUID, List<SessionItem>> itemsByVocabulary = items
            .stream()
            .collect(Collectors.groupingBy(item -> item.getVocabulary().getId()));

        // Calculate item-specific statistics
        final List<String> difficultTerms = itemsByVocabulary
            .entrySet()
            .stream()
            .filter(entry -> calculateAccuracy(entry.getValue()) < 50.0)
            .map(entry -> entry.getValue().get(0).getVocabulary().getTerm())
            .sorted()
            .toList();

        final List<String> masteredTerms = itemsByVocabulary
            .entrySet()
            .stream()
            .filter(entry -> calculateAccuracy(entry.getValue()) == 100.0)
            .map(entry -> entry.getValue().get(0).getVocabulary().getTerm())
            .sorted()
            .toList();

        return SessionItemAnalysisResponse
            .builder()
            .totalItems(totalItems)
            .correctItems(correctItems)
            .accuracyRate(accuracyRate)
            .averageResponseTimeMs(averageResponseTimeMs)
            .difficultTerms(difficultTerms)
            .masteredTerms(masteredTerms)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySessionResponse> getActiveSessionsByUser(UUID userId) {
        log.debug("Getting active sessions for user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound(messageSource, EntityNames.USER, userId);
        }

        final List<StudySession> activeSessions = studySessionRepository.findActiveSessionsByUserId(userId);
        return studySessionMapper.toDtoList(activeSessions);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalStudyTimeInSeconds(UUID userId, LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

        log.debug("Getting total study time for user ID: {} between {} and {}", userId, startDate, endDate);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound(messageSource, EntityNames.USER, userId);
        }

        // Validate date range
        if (startDate.isAfter(endDate)) {
            log.error("Invalid date range: start date {} is after end date {}", startDate, endDate);
            throw KardioException.validationError(messageSource, "error.daterange.invalid");
        }

        // Convert LocalDate to LocalDateTime for database query
        final LocalDateTime startDateTime = startDate.atStartOfDay();
        final LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        final Long totalSeconds = studySessionRepository.getTotalStudyTimeInSeconds(userId, startDateTime, endDateTime);

        return totalSeconds != null ? totalSeconds : 0L;
    }

    @Override
    @Transactional
    public SuccessResponse deleteSession(UUID id, UUID userId) {
        log.info("Deleting session with ID: {} by user ID: {}", id, userId);

        final StudySession session = findSessionByIdAndValidateOwnership(id, userId);

        // Delete session items first
        sessionItemRepository.deleteBySessionId(id);

        // Delete session
        studySessionRepository.delete(session);

        log.info("Session deleted successfully: {}", id);
        return SuccessResponse.of(messageSource.getMessage("success.deleted", new Object[]{
                messageSource.getMessage("entity.session", null, LocaleContextHolder.getLocale())
        }, LocaleContextHolder.getLocale()));
    }

    /**
     * Helper method to find session by ID and validate ownership.
     *
     * @param id     Session ID
     * @param userId User ID
     * @return The session
     * @throws KardioException if session not found or user is not the owner
     */
    private StudySession findSessionByIdAndValidateOwnership(UUID id, UUID userId) {
        final StudySession session = studySessionRepository.findById(id).orElseThrow(() -> {
            log.error("Study session not found with ID: {}", id);
            return KardioException.resourceNotFound(messageSource, "entity.session", id);
        });

        // Check ownership
        if (!session.getUser().getId().equals(userId)) {
            log.error("User {} is not the owner of session {}", userId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    ErrorMessages.FORBIDDEN_OWNER,
                    "access",
                    messageSource.getMessage("entity.session", null, LocaleContextHolder.getLocale()));
        }

        return session;
    }

    /**
     * Updates a user's streak.
     *
     * @param user The user
     */
    private void updateUserStreak(User user) {
        try {
            streakRepository.findByUserId(user.getId()).ifPresent(streak -> {
                streak.recordActivity();
                streakRepository.save(streak);
                log.debug("Updated streak for user: {}", user.getId());
            });
        } catch (Exception e) {
            // Non-critical operation, log error but don't interrupt main flow
            log.error("Failed to update streak for user {}: {}", user.getId(), e.getMessage());
        }
    }

    /**
     * Updates a module's last studied time.
     *
     * @param module The module
     */
    private void updateModuleLastStudiedTime(StudyModule module) {
        try {
            module.setLastStudiedAt(LocalDateTime.now());
            studyModuleRepository.save(module);
            log.debug("Updated last studied time for module: {}", module.getId());
        } catch (Exception e) {
            // Non-critical operation, log error but don't interrupt main flow
            log.error("Failed to update last studied time for module {}: {}", module.getId(), e.getMessage());
        }
    }

    /**
     * Calculates accuracy rate for a list of session items.
     *
     * @param items List of session items
     * @return Accuracy rate (0-100)
     */
    private double calculateAccuracy(List<SessionItem> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }

        final int correctCount = (int) items.stream().filter(item -> Boolean.TRUE.equals(item.getIsCorrect())).count();

        return (double) correctCount / items.size() * 100;
    }

}