package com.kardio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kardio.dto.AuthRequest;
import com.kardio.dto.AuthResponse;
import com.kardio.dto.RegisterRequest;
import com.kardio.dto.UserResponse;
import com.kardio.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
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
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
		final AuthResponse response = authService.authenticate(request);
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
	public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
		final boolean isValid = authService.validateToken(token);
		return ResponseEntity.ok(isValid);
	}
}