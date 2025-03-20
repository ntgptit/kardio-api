package com.kardio.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kardio.entity.SessionItem;

@Repository
public interface SessionItemRepository extends JpaRepository<SessionItem, UUID> {

    /**
     * Finds items by session ID.
     *
     * @param sessionId Session ID
     * @return List of session items
     */
    List<SessionItem> findBySessionId(UUID sessionId);

    /**
     * Finds items by session ID and vocabulary ID.
     *
     * @param sessionId    Session ID
     * @param vocabularyId Vocabulary ID
     * @return List of session items
     */
    List<SessionItem> findBySessionIdAndVocabularyId(UUID sessionId, UUID vocabularyId);

    /**
     * Finds items by vocabulary ID.
     *
     * @param vocabularyId Vocabulary ID
     * @return List of session items
     */
    List<SessionItem> findByVocabularyId(UUID vocabularyId);

    /**
     * Counts items by session ID.
     *
     * @param sessionId Session ID
     * @return Count of items
     */
    long countBySessionId(UUID sessionId);

    /**
     * Counts correct items by session ID.
     *
     * @param sessionId Session ID
     * @return Count of correct items
     */
    @Query("SELECT COUNT(i) FROM SessionItem i WHERE i.session.id = :sessionId AND i.isCorrect = true")
    long countCorrectItemsBySessionId(@Param("sessionId") UUID sessionId);

    /**
     * Gets average response time by session ID.
     *
     * @param sessionId Session ID
     * @return Average response time in milliseconds
     */
    @Query("SELECT AVG(i.responseTimeMs) FROM SessionItem i WHERE i.session.id = :sessionId AND i.responseTimeMs IS NOT NULL")
    Double getAverageResponseTimeBySessionId(@Param("sessionId") UUID sessionId);

    /**
     * Deletes items by session ID.
     *
     * @param sessionId Session ID
     */
    @Modifying
    void deleteBySessionId(UUID sessionId);
}