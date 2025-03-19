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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kardio.exception.ApiError;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
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

    // Paths that should be excluded from JWT validation
    private final RequestMatcher publicPaths = new OrRequestMatcher(
        new AntPathRequestMatcher("/api/v1/auth/login"),
        new AntPathRequestMatcher("/api/v1/auth/register"),
        new AntPathRequestMatcher("/api/v1/auth/validate"),
        new AntPathRequestMatcher("/swagger-ui/**"),
        new AntPathRequestMatcher("/v3/api-docs/**"),
        new AntPathRequestMatcher("/actuator/health"),
        new AntPathRequestMatcher("/error"));

    /**
     * Determine if a request should not be filtered.
     *
     * @param request The HTTP request
     * @return true if the request should be skipped
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return publicPaths.matches(request);
    }

    /**
     * Filters incoming requests to validate JWT tokens and set up security context.
     *
     * @param request     The HTTP request
     * @param response    The HTTP response
     * @param filterChain The filter chain
     * @throws ServletException If a servlet exception occurs
     * @throws IOException      If an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException,
                IOException {

        try {
            // Extract JWT token from Authorization header
            final String token = extractTokenFromRequest(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                processToken(token, response);
            }

            // Continue with filter chain if no error was sent
            filterChain.doFilter(request, response);

        } catch (AuthenticationException e) {
            log.error("Authentication error: {}", e.getMessage());
            sendErrorResponse(response, e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter: {}", e.getMessage(), e);
            sendErrorResponse(response, "Internal security error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Process and validate the JWT token.
     *
     * @param token    The JWT token to validate
     * @param response The HTTP response
     * @return true if processing succeeded, false if an error response was sent
     * @throws IOException If an I/O error occurs when sending an error response
     */
    private boolean processToken(String token, HttpServletResponse response) throws IOException {
        try {
            // Validate token and extract username
            if (tokenProvider.validateToken(token)) {
                final String username = tokenProvider.getUsernameFromToken(token);

                // Load user details
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Create authentication token
                final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Set authentication for user: {}", username);
                return true;
            }
            return true; // Token is valid but didn't meet criteria
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            sendErrorResponse(response, "Token has expired", HttpStatus.UNAUTHORIZED);
            return false;
        } catch (MalformedJwtException | SignatureException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            sendErrorResponse(response, "Invalid token format", HttpStatus.UNAUTHORIZED);
            return false;
        } catch (JwtException e) {
            log.error("JWT token validation error: {}", e.getMessage());
            sendErrorResponse(response, "Token validation failed", HttpStatus.UNAUTHORIZED);
            return false;
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
     * @param status       The HTTP status
     * @throws IOException If an I/O error occurs
     */
    private void sendErrorResponse(HttpServletResponse response, String errorMessage, HttpStatus status)
            throws IOException {

        // Create error response
        final ApiError errorResponse = ApiError
            .builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message("JWT authentication failed: " + errorMessage)
            .path("") // Path not available in filter
            .build();

        // Write error response
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}