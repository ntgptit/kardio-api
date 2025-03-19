package com.kardio.dto.classroom;

import com.kardio.entity.enums.MemberRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a class member
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMemberUpdateRequest {
	@NotNull(message = "Role is required")
	private MemberRole role;
}