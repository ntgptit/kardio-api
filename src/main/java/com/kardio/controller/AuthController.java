package com.kardio.controller;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kardio.dto.auth.AuthRequest;
import com.kardio.dto.auth.AuthResponse;
import com.kardio.dto.auth.RefreshTokenRequest;
import com.kardio.dto.auth.RegisterRequest;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.dto.user.UserResponse;
import com.kardio.security.CustomUserDetails;
import com.kardio.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

/**
 * Controller for authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user.
     *
     * @param request The registration request
     * @return The created user
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "Email already in use")
    public ResponseEntity<UserResponse> register(
            @Valid
            @RequestBody RegisterRequest request) {
        Objects.requireNonNull(request, "Registration request cannot be null");
        final UserResponse user = authService.register(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request The authentication request
     * @return The authentication response with token
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and get a JWT token")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody AuthRequest request) {
        Objects.requireNonNull(request, "Authentication request cannot be null");
        final AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an authentication token.
     *
     * @param request The refresh token request
     * @return The new authentication response with token
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh an authentication token using a refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    @ApiResponse(responseCode = "401", description = "Refresh token is expired or invalid")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid
            @RequestBody RefreshTokenRequest request) {
        Objects.requireNonNull(request, "Refresh token request cannot be null");
        final AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a JWT token.
     *
     * @param token The JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate a JWT token")
    @ApiResponse(responseCode = "200", description = "Token validation result")
    @ApiResponse(responseCode = "400", description = "Token parameter is missing")
    public ResponseEntity<Boolean> validateToken(
            @RequestParam
            @NotBlank(message = "Token is required")
            @Parameter(description = "JWT token to validate", required = true) String token) {

        Objects.requireNonNull(token, "Token cannot be null");
        final boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }

    /**
     * Get current authenticated user.
     *
     * @param userDetails The authenticated user details
     * @return The user information
     */
    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    @ApiResponse(responseCode = "200", description = "Current user information")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity
            .ok(
                UserResponse
                    .builder()
                    .id(userDetails.getUser().getId())
                    .email(userDetails.getUsername())
                    .firstName(userDetails.getUser().getFirstName())
                    .lastName(userDetails.getUser().getLastName())
                    .displayName(userDetails.getUser().getDisplayName())
                    .roles(
                        userDetails
                            .getAuthorities()
                            .stream()
                            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                            .toList())
                    .build());
    }

    /**
     * Logs out the current user by invalidating their token.
     *
     * @return Success response
     */
    @PostMapping("/logout")
    @Operation(summary = "Log out the current user")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<SuccessResponse> logout() {
        return ResponseEntity.ok(SuccessResponse.of("Logged out successfully"));
    }
}