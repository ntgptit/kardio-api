package com.kardio.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kardio.entity.StarredItem;

/**
 * Repository for StarredItem entity.
 */
@Repository
public interface StarredItemRepository extends JpaRepository<StarredItem, UUID> {

    /**
     * Finds a starred item by user ID and vocabulary ID.
     *
     * @param userId       User ID
     * @param vocabularyId Vocabulary ID
     * @return Optional containing the starred item if found
     */
    Optional<StarredItem> findByUserIdAndVocabularyId(UUID userId, UUID vocabularyId);

    /**
     * Checks if a starred item exists by user ID and vocabulary ID.
     *
     * @param userId       User ID
     * @param vocabularyId Vocabulary ID
     * @return true if the item exists, false otherwise
     */
    boolean existsByUserIdAndVocabularyId(UUID userId, UUID vocabularyId);

    /**
     * Finds starred items by user ID and vocabulary IDs.
     *
     * @param userId        User ID
     * @param vocabularyIds List of vocabulary IDs
     * @return List of starred items
     */
    List<StarredItem> findByUserIdAndVocabularyIdIn(UUID userId, List<UUID> vocabularyIds);

    /**
     * Deletes a starred item by user ID and vocabulary ID.
     *
     * @param userId       User ID
     * @param vocabularyId Vocabulary ID
     */
    void deleteByUserIdAndVocabularyId(UUID userId, UUID vocabularyId);
}