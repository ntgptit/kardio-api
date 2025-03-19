package com.kardio.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

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
import com.kardio.dto.auth.AuthRequest;
import com.kardio.dto.auth.AuthResponse;
import com.kardio.dto.user.UserResponse;
import com.kardio.entity.User;
import com.kardio.exception.ApiError;
import com.kardio.mapper.UserMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
    private final UserMapper userMapper;

    /**
     * Attempt authentication based on credentials in the request.
     *
     * @param request  the HTTP request containing login credentials
     * @param response the HTTP response
     * @return Authentication object if successful
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        try {
            // Parse login credentials from request body
            final AuthRequest authRequest = objectMapper.readValue(request.getInputStream(), AuthRequest.class);

            // Validate request
            Objects.requireNonNull(authRequest, "Authentication request cannot be null");
            Objects.requireNonNull(authRequest.getEmail(), "Email cannot be null");
            Objects.requireNonNull(authRequest.getPassword(), "Password cannot be null");

            log.debug("Attempting authentication for user: {}", authRequest.getEmail());

            // Create authentication token with credentials
            final Authentication authentication = new UsernamePasswordAuthenticationToken(
                authRequest.getEmail(),
                authRequest.getPassword());

            // Authenticate using the authentication manager
            return authenticationManager.authenticate(authentication);

        } catch (IOException e) {
            log.error("Failed to parse authentication request", e);
            throw new AuthenticationServiceException("Failed to parse authentication request", e);
        } catch (NullPointerException e) {
            log.error("Invalid authentication request: {}", e.getMessage());
            throw new AuthenticationServiceException("Invalid authentication request: " + e.getMessage(), e);
        }
    }

    /**
     * Handle successful authentication by generating JWT token.
     *
     * @param request    the HTTP request
     * @param response   the HTTP response
     * @param chain      the filter chain
     * @param authResult the successful authentication result
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        // Get user details from authentication result
        final UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        log.info("Authentication successful for user: {}", userDetails.getUsername());

        // Get user from principal if available (custom UserDetails implementation)
        User user = null;
        if (authResult.getPrincipal() instanceof CustomUserDetails) {
            user = ((CustomUserDetails) authResult.getPrincipal()).getUser();
        }

        // Generate JWT token
        final String token = tokenProvider.generateToken(authResult);

        // Create authentication response
        final UserResponse userResponse = user != null ? userMapper.toDto(user)
                : UserResponse
                    .builder()
                    .email(userDetails.getUsername())
                    .roles(
                        userDetails
                            .getAuthorities()
                            .stream()
                            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                            .toList())
                    .build();

        final AuthResponse authResponse = AuthResponse.builder().token(token).user(userResponse).build();

        // Return token in response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.OK.value());
        objectMapper.writeValue(response.getOutputStream(), authResponse);
    }

    /**
     * Handle failed authentication.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param failed   the authentication exception
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {

        log.warn("Authentication failed: {}", failed.getMessage());

        // Create detailed error response
        final ApiError errorResponse = ApiError
            .builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
            .message("Authentication failed: " + failed.getMessage())
            .path(request.getRequestURI())
            .build();

        // Return error in response
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}