package com.kardio.dto.classroom;

import java.util.UUID;

import com.kardio.entity.enums.MemberRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adding a member to a class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMemberAddRequest {
	@NotNull(message = "User ID is required")
	private UUID userId;

	@NotNull(message = "Role is required")
	private MemberRole role;
}
