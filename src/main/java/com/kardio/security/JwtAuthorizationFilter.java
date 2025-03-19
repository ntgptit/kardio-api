package com.kardio.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kardio.exception.ApiError;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter for JWT authorization. Validates JWT tokens and sets up security
 * context for authenticated requests.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;
	private final UserDetailsService userDetailsService;
	private final ObjectMapper objectMapper;

	private static final String BEARER_PREFIX = "Bearer ";

	/**
	 * Filters incoming requests to validate JWT tokens and set up security context.
	 */
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		try {
			// Extract JWT token from Authorization header
			final String token = extractTokenFromRequest(request);

			if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				// Validate token and extract username
				if (tokenProvider.validateToken(token)) {
					final String username = tokenProvider.getUsernameFromToken(token);

					// Load user details
					final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

					// Create authentication token
					final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());

					// Set authentication in security context
					SecurityContextHolder.getContext().setAuthentication(authToken);
					log.debug("Set authentication for user: {}", username);
				}
			}

			// Continue with filter chain
			filterChain.doFilter(request, response);

		} catch (JwtException | AuthenticationException e) {
			log.error("JWT authentication error: {}", e.getMessage());
			sendErrorResponse(response, e.getMessage());
		}
	}

	/**
	 * Extracts JWT token from request Authorization header.
	 * 
	 * @param request The HTTP request
	 * @return The JWT token or null if not found
	 */
	private String extractTokenFromRequest(HttpServletRequest request) {
		final String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length());
		}

		return null;
	}

	/**
	 * Sends error response when authentication fails.
	 * 
	 * @param response     The HTTP response
	 * @param errorMessage The error message
	 */
	private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
		// Create error response
		final ApiError errorResponse = ApiError.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.UNAUTHORIZED.value()).error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
				.message("JWT authentication failed: " + errorMessage).path("") // Path not available in filter
				.build();

		// Write error response
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), errorResponse);
	}
}