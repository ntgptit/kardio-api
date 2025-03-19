package com.kardio.dto.classroom;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kardio.dto.user.UserResponse;
import com.kardio.entity.enums.MemberRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for class member response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMemberResponse {
	private UUID id;
	private UUID userId;
	private UUID classId;
	private String className;
	private UserResponse user;
	private MemberRole role;
	private LocalDateTime joinedAt;
}