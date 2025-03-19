package com.kardio.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.kardio.security.JwtTokenProvider;
import com.kardio.service.AuthService;

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
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already in use: {}", request.getEmail());
            throw KardioException.resourceAlreadyExists("User", "email", request.getEmail());
        }

        Role userRole = roleRepository.findByName(Role.RoleName.USER).orElseThrow(() -> {
            log.error("Default user role not found");
            return new KardioException("Default user role not found", HttpStatus.INTERNAL_SERVER_ERROR);
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

        // Sử dụng phương thức toDto từ generic mapper
        return userMapper.toDto(savedUser);
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authenticating user: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);

            User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> {
                log.error("User not found after authentication: {}", request.getEmail());
                return KardioException.resourceNotFound("User", request.getEmail());
            });

            log.info("User authenticated successfully: {}", user.getEmail());

            // Sử dụng phương thức toDto từ generic mapper
            return AuthResponse.builder().token(token).user(userMapper.toDto(user)).build();
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user {}: Invalid credentials", request.getEmail());
            throw new KardioException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }
}