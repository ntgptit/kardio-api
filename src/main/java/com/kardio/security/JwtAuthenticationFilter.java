package com.kardio.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kardio.dto.AuthResponse;
import com.kardio.dto.LoginRequest;
import com.kardio.dto.UserResponse;
import com.kardio.exception.ApiError;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter for JWT authentication. Processes login requests and generates JWT
 * tokens upon successful authentication.
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider tokenProvider;
	private final ObjectMapper objectMapper;

	/**
	 * Attempt authentication based on credentials in the request.
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			// Parse login credentials from request body
			final LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
			log.debug("Attempting authentication for user: {}", loginRequest.getUsername());

			// Create authentication token with credentials
			final Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
					loginRequest.getPassword());

			// Authenticate using the authentication manager
			return authenticationManager.authenticate(authentication);

		} catch (IOException e) {
			log.error("Failed to parse authentication request", e);
			throw new AuthenticationServiceException("Failed to parse authentication request", e);
		}
	}

	/**
	 * Handle successful authentication by generating JWT token.
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException {

		final UserDetails userDetails = (UserDetails) authResult.getPrincipal();
		log.info("Authentication successful for user: {}", userDetails.getUsername());

		// Generate JWT token
		final String token = tokenProvider.generateToken(userDetails);

		// Create authentication response
		final AuthResponse authResponse = new AuthResponse(token, new UserResponse(null, token, token, token, null));

		// Return token in response
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), authResponse);
	}

	/**
	 * Handle failed authentication.
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException {

		log.warn("Authentication failed: {}", failed.getMessage());

		// Create error response
		final ApiError errorResponse = ApiError.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.UNAUTHORIZED.value()).error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
				.message("Authentication failed: " + failed.getMessage()).path(request.getRequestURI()).build();

		// Return error in response
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), errorResponse);
	}

}