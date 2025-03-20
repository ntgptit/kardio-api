package com.kardio.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kardio.entity.StudySession;
import com.kardio.entity.enums.SessionType;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

    /**
     * Finds sessions by user ID with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Page of study sessions
     */
    Page<StudySession> findByUserId(UUID userId, Pageable pageable);

    /**
     * Finds sessions by module ID with pagination.
     *
     * @param moduleId Module ID
     * @param pageable Pagination information
     * @return Page of study sessions
     */
    Page<StudySession> findByModuleId(UUID moduleId, Pageable pageable);

    /**
     * Finds sessions by user ID and module ID with pagination.
     *
     * @param userId   User ID
     * @param moduleId Module ID
     * @param pageable Pagination information
     * @return Page of study sessions
     */
    Page<StudySession> findByUserIdAndModuleId(UUID userId, UUID moduleId, Pageable pageable);

    /**
     * Finds sessions by user ID and session type with pagination.
     *
     * @param userId      User ID
     * @param sessionType Session type
     * @param pageable    Pagination information
     * @return Page of study sessions
     */
    Page<StudySession> findByUserIdAndSessionType(UUID userId, SessionType sessionType, Pageable pageable);

    /**
     * Finds active (not ended) sessions for a user.
     *
     * @param userId User ID
     * @return List of active study sessions
     */
    @Query("SELECT s FROM StudySession s WHERE s.user.id = :userId AND s.endTime IS NULL")
    List<StudySession> findActiveSessionsByUserId(@Param("userId") UUID userId);

    /**
     * Gets recent sessions for a user.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Page of study sessions
     */
    @Query("SELECT s FROM StudySession s WHERE s.user.id = :userId ORDER BY s.startTime DESC")
    Page<StudySession> findRecentSessionsByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Gets total study time for a user within a date range.
     *
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @return Total study time in seconds
     */
    @Query("SELECT SUM(FUNCTION('TIMESTAMPDIFF', SECOND, s.startTime, "
            + "CASE WHEN s.endTime IS NULL THEN CURRENT_TIMESTAMP ELSE s.endTime END)) " + "FROM StudySession s "
            + "WHERE s.user.id = :userId " + "AND s.startTime >= :startDate "
            + "AND (s.endTime IS NULL OR s.endTime <= :endDate)")
    Long getTotalStudyTimeInSeconds(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Gets session count by type for a user.
     *
     * @param userId User ID
     * @return Array of [sessionType, count]
     */
    @Query("SELECT s.sessionType, COUNT(s) FROM StudySession s " + "WHERE s.user.id = :userId "
            + "GROUP BY s.sessionType")
    List<Object[]> countSessionsByType(@Param("userId") UUID userId);

    /**
     * Calculates average accuracy by user and module.
     *
     * @param userId   User ID
     * @param moduleId Module ID
     * @return Average accuracy rate or null if no sessions
     */
    @Query("SELECT AVG(s.correctItems * 100.0 / NULLIF(s.totalItems, 0)) " + "FROM StudySession s "
            + "WHERE s.user.id = :userId AND s.module.id = :moduleId " + "AND s.totalItems > 0")
    Double getAverageAccuracyByUserAndModule(@Param("userId") UUID userId, @Param("moduleId") UUID moduleId);
}