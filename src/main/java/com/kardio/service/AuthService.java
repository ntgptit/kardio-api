package com.kardio.service;

import com.kardio.dto.auth.AuthRequest;
import com.kardio.dto.auth.AuthResponse;
import com.kardio.dto.auth.RegisterRequest;
import com.kardio.dto.user.UserResponse;

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

    /**
     * Refreshes an authentication token.
     *
     * @param refreshToken The refresh token
     * @return The new authentication response with tokens
     */
    AuthResponse refreshToken(String refreshToken);
}