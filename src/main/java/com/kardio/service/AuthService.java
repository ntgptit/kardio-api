package com.kardio.service;

import com.kardio.dto.AuthRequest;
import com.kardio.dto.AuthResponse;
import com.kardio.dto.RegisterRequest;
import com.kardio.dto.UserResponse;

/**
 * Service interface for authentication-related operations.
 */
public interface AuthService {

	/**
	 * Registers a new user.
	 *
	 * @param request The registration request
	 * @return The created user response
	 */
	UserResponse register(RegisterRequest request);

	/**
	 * Authenticates a user and returns a JWT token.
	 *
	 * @param request The authentication request
	 * @return The authentication response with token
	 */
	AuthResponse authenticate(AuthRequest request);

	/**
	 * Validates a JWT token.
	 *
	 * @param token The JWT token to validate
	 * @return true if the token is valid, false otherwise
	 */
	boolean validateToken(String token);
}
