package com.kardio.dto.settings;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user setting response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {
	private UUID id;
	private String settingKey;
	private String settingValue;
	private LocalDateTime updatedAt;
}