package com.kardio.dto.settings;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a user setting
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsUpsertRequest {
	@NotBlank(message = "Setting key is required")
	private String settingKey;

	private String settingValue;
}