package com.kardio.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kardio.entity.Streak;

@Repository
public interface StreakRepository extends JpaRepository<Streak, UUID> {

    /**
     * Finds a streak by user ID.
     *
     * @param userId User ID
     * @return Optional containing the streak if found
     */
    Optional<Streak> findByUserId(UUID userId);

    /**
     * Checks if a streak exists by user ID.
     *
     * @param userId User ID
     * @return true if a streak exists, false otherwise
     */
    boolean existsByUserId(UUID userId);
}