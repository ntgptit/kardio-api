package com.kardio.dto.classroom;

import java.util.UUID;

import com.kardio.dto.user.UserResponse;
import com.kardio.entity.enums.MemberRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for class summary (used in lists)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSummaryResponse {
	private UUID id;
	private String name;
	private UserResponse creator;
	private Integer memberCount;
	private MemberRole userRole;
}
