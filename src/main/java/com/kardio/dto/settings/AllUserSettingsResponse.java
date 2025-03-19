package com.kardio.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for all user settings response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllUserSettingsResponse {
	private UserSettingsResponse[] settings;
}