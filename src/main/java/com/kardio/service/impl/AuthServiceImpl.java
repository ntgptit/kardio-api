package com.kardio.service.impl;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.dto.auth.AuthRequest;
import com.kardio.dto.auth.AuthResponse;
import com.kardio.dto.auth.RegisterRequest;
import com.kardio.dto.user.UserResponse;
import com.kardio.entity.Role;
import com.kardio.entity.User;
import com.kardio.exception.KardioException;
import com.kardio.mapper.UserMapper;
import com.kardio.repository.RoleRepository;
import com.kardio.repository.UserRepository;
import com.kardio.security.CustomUserDetailsService;
import com.kardio.security.JwtTokenProvider;
import com.kardio.service.AuthService;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserMapper userMapper;
    private final MessageSource messageSource;

    @Transactional
    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already in use: {}", request.getEmail());
            throw KardioException.resourceAlreadyExists(messageSource, "entity.user", "email", request.getEmail());
        }

        Role userRole = roleRepository.findByName(Role.RoleName.USER).orElseThrow(() -> {
            log.error("Default user role not found");
            return new KardioException(
                messageSource
                    .getMessage(
                        "error.role.notfound",
                        null,
                        "Default user role not found",
                        LocaleContextHolder.getLocale()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        });

        User user = User
            .builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .active(true)
            .build();

        user.addRole(userRole);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        return userMapper.toDto(savedUser);
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authenticating user: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate both access and refresh tokens
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> {
                log.error("User not found after authentication: {}", request.getEmail());
                return KardioException.resourceNotFound(messageSource, "entity.user", request.getEmail());
            });

            log.info("User authenticated successfully: {}", user.getEmail());

            return AuthResponse
                .builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toDto(user))
                .build();
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user {}: Invalid credentials", request.getEmail());
            throw new KardioException(
                messageSource
                    .getMessage(
                        "error.auth.invalid",
                        null,
                        "Invalid email or password",
                        LocaleContextHolder.getLocale()),
                HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        try {
            // Validate the refresh token
            if (!tokenProvider.validateToken(refreshToken)) {
                log.error("Invalid refresh token");
                throw new KardioException(
                    messageSource
                        .getMessage(
                            "error.token.invalid",
                            null,
                            "Invalid refresh token",
                            LocaleContextHolder.getLocale()),
                    HttpStatus.UNAUTHORIZED);
            }

            // Check if it's actually a refresh token
            if (!tokenProvider.isRefreshToken(refreshToken)) {
                log.error("Token is not a refresh token");
                throw new KardioException(
                    messageSource
                        .getMessage(
                            "error.token.notrefresh",
                            null,
                            "Token is not a refresh token",
                            LocaleContextHolder.getLocale()),
                    HttpStatus.UNAUTHORIZED);
            }

            // Extract username from token
            String username = tokenProvider.getUsernameFromToken(refreshToken);

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Create authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

            // Generate new tokens
            String newAccessToken = tokenProvider.generateToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

            // Get user data
            User user = userRepository.findByEmail(username).orElseThrow(() -> {
                log.error("User not found when refreshing token: {}", username);
                return KardioException.resourceNotFound(messageSource, "entity.user", username);
            });

            log.info("Token refreshed successfully for user: {}", username);

            return AuthResponse
                .builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userMapper.toDto(user))
                .build();

        } catch (JwtException e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new KardioException(
                messageSource
                    .getMessage(
                        "error.token.refresh",
                        null,
                        "Failed to refresh token",
                        LocaleContextHolder.getLocale()),
                HttpStatus.UNAUTHORIZED);
        }
    }
}