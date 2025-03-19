package com.kardio.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for batch updating settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsBatchUpdateRequest {
	private UserSettingsUpsertRequest[] settings;
}
