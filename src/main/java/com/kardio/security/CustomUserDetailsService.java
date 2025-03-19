package com.kardio.security;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.entity.User;
import com.kardio.exception.KardioException;
import com.kardio.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom implementation of UserDetailsService. Loads user-specific data for
 * Spring Security.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by username (email).
     *
     * @param username The username (email) to load
     * @return A UserDetails object
     * @throws UsernameNotFoundException if the user is not found
     * @throws KardioException           if the user account is disabled
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Validate input
        Objects.requireNonNull(username, "Username cannot be null");

        log.debug("Loading user by username: {}", username);

        // Find user by email with roles (using optimized query to avoid N+1 issue)
        final User user = userRepository.findByEmailWithRoles(username).orElseThrow(() -> {
            log.warn("User not found with email: {}", username);
            return new UsernameNotFoundException("User not found with email: " + username);
        });

        // Check if user is active
        if (!user.isActive()) {
            log.warn("User is inactive: {}", username);
            throw new KardioException("Account is disabled", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        // Create UserDetails
        return buildUserDetails(user);
    }

    /**
     * Builds a UserDetails object from a User entity.
     *
     * @param user The user entity
     * @return A UserDetails object
     */
    private UserDetails buildUserDetails(User user) {
        // Convert roles to authorities
        final List<SimpleGrantedAuthority> authorities = user
            .getRoles()
            .stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());

        // Create CustomUserDetails with simplified constructor
        return new CustomUserDetails(user, authorities);
    }
}