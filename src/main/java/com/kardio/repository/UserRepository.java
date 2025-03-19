package com.kardio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kardio.entity.User;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Finds a user by email.
	 *
	 * @param email The email to search for
	 * @return An Optional containing the user if found
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Checks if a user exists with the given email.
	 *
	 * @param email The email to check
	 * @return true if a user exists, false otherwise
	 */
	boolean existsByEmail(String email);

	/**
	 * Finds a user by email, including roles, with a single query. Uses a custom
	 * query to avoid N+1 issues.
	 *
	 * @param email The email to search for
	 * @return An Optional containing the user if found
	 */
	@Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
	Optional<User> findByEmailWithRoles(String email);
}