package com.kardio.dto.classroom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.kardio.dto.user.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for detailed class response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassDetailedResponse {
	private UUID id;
	private String name;
	private String description;
	private UserResponse creator;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Integer memberCount;
	private Integer moduleCount;
	private List<ClassMemberResponse> members;
	private List<ClassModuleResponse> modules;
}