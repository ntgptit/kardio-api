package com.kardio.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    Page<Vocabulary> findStarredByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds vocabularies by module ID and IDs.
     *
     * @param moduleId Module ID
     * @param ids      Vocabulary IDs
     * @return List of vocabularies
     */
    List<Vocabulary> findByModuleIdAndIdIn(UUID moduleId, List<UUID> ids);

    /**
     * Efficiently count vocabularies for multiple modules at once.
     *
     * @param moduleIds Collection of module IDs
     * @return List of arrays containing [moduleId, count]
     */
    @Query("SELECT v.module.id as moduleId, COUNT(v) as count " + "FROM Vocabulary v "
            + "WHERE v.module.id IN :moduleIds AND v.deletedAt IS NULL " + "GROUP BY v.module.id")
    List<Object[]> countVocabulariesForModules(@Param("moduleIds") Collection<UUID> moduleIds);

    /**
     * Finds vocabularies with their statistics in a single query.
     *
     * @param moduleId Module ID
     * @param userId   User ID (optional, can be null)
     * @param pageable Pagination information
     * @return Page of vocabulary statistics [vocabulary, starred, correctCount,
     *         incorrectCount, status]
     */
    @Query("SELECT v, " + "CASE WHEN s.id IS NOT NULL THEN true ELSE false END as starred, "
            + "COALESCE(lp.correctCount, 0) as correctCount, " + "COALESCE(lp.incorrectCount, 0) as incorrectCount, "
            + "lp.status as status " + "FROM Vocabulary v "
            + "LEFT JOIN StarredItem s ON s.vocabulary.id = v.id AND s.user.id = :userId "
            + "LEFT JOIN LearningProgress lp ON lp.vocabulary.id = v.id AND lp.user.id = :userId "
            + "WHERE v.module.id = :moduleId AND v.deletedAt IS NULL")
    Page<Object[]> findByModuleWithStats(
            @Param("moduleId") UUID moduleId,
            @Param("userId") UUID userId,
            Pageable pageable);
}