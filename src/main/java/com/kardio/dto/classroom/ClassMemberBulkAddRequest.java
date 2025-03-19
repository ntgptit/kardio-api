package com.kardio.dto.classroom;

import java.util.UUID;

import com.kardio.entity.enums.MemberRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bulk adding members
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMemberBulkAddRequest {
	@NotNull(message = "User IDs are required")
	private UUID[] userIds;

	@NotNull(message = "Role is required")
	private MemberRole role;
}
