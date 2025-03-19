package com.kardio.dto.classroom;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kardio.dto.user.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for class response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponse {
	private UUID id;
	private String name;
	private String description;
	private UserResponse creator;
	private LocalDateTime createdAt;
	private Integer memberCount;
	private Integer moduleCount;
}
