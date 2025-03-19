package com.kardio.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kardio.entity.Vocabulary;

/**
 * Repository for Vocabulary entity.
 */
@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, UUID> {

    /**
     * Finds vocabularies by module ID.
     *
     * @param moduleId Module ID
     * @param pageable Pagination information
     * @return Page of vocabularies
     */
    Page<Vocabulary> findByModuleId(UUID moduleId, Pageable pageable);

    /**
     * Counts vocabularies by module ID.
     *
     * @param moduleId Module ID
     * @return Count of vocabularies
     */
    long countByModuleId(UUID moduleId);

    /**
     * Finds vocabularies by term containing the given string (case insensitive).
     *
     * @param term     Term to search for
     * @param pageable Pagination information
     * @return Page of vocabularies
     */
    Page<Vocabulary> findByTermContainingIgnoreCase(String term, Pageable pageable);

    /**
     * Finds starred vocabularies by user ID.
     *
     * @param userId   User ID
     * @param pageable Pagination information
     * @return Page of starred vocabularies
     */
    @Query("SELECT v FROM Vocabulary v JOIN StarredItem s ON s.vocabulary.id = v.id "
            + "WHERE s.user.id = :userId AND v.deletedAt IS NULL")
    Page<Vocabulary> findStarredByUserId(UUID userId, Pageable pageable);

    /**
     * Finds vocabularies by module ID and IDs.
     *
     * @param moduleId Module ID
     * @param ids      Vocabulary IDs
     * @return List of vocabularies
     */
    List<Vocabulary> findByModuleIdAndIdIn(UUID moduleId, List<UUID> ids);
}