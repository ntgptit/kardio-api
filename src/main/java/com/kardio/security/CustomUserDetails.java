package com.kardio.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.kardio.entity.User;

import lombok.Getter;

/**
 * Custom UserDetails implementation that holds a reference to the original User
 * entity.
 */
@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 1L;

    private final User user;

    /**
     * Constructs a CustomUserDetails from a User entity
     *
     * @param user        The original User entity
     * @param authorities The authorities granted to the user
     */
    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        super(
            user.getEmail(),
            user.getPassword(),
            user.isActive(),
            true, // accountNonExpired - Không cần thiết truyền vào, mặc định true
            true, // credentialsNonExpired - Không cần thiết truyền vào, mặc định true
            true, // accountNonLocked - Không cần thiết truyền vào, mặc định true
            authorities);
        this.user = user;
    }
}