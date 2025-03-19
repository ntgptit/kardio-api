package com.kardio.dto.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user response (public data)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
	private UUID id;
	private String email;
	private String firstName;
	private String lastName;
	private String displayName;
	private LocalDateTime createdAt;
	private List<String> roles;
}