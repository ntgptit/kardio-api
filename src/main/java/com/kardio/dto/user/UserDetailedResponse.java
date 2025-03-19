package com.kardio.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for detailed user response (admin view)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailedResponse {
	private UUID id;
	private String email;
	private String displayName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
}