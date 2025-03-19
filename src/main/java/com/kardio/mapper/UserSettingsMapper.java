package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.settings.UserSettingsResponse;
import com.kardio.dto.settings.UserSettingsUpsertRequest;
import com.kardio.entity.User;
import com.kardio.entity.UserSettings;

/**
 * Mapper for UserSettings entity.
 * Handles mapping between UserSettings entities and related DTOs.
 */
@Component
public class UserSettingsMapper extends AbstractGenericMapper<UserSettings, UserSettingsResponse> {

    @Override
    protected UserSettingsResponse mapToDto(UserSettings entity) {
        if (entity == null) {
            return null;
        }

        return UserSettingsResponse
            .builder()
            .id(entity.getId())
            .settingKey(entity.getSettingKey())
            .settingValue(entity.getSettingValue())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    @Override
    protected UserSettings mapToEntity(UserSettingsResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // User would need to be set separately
        UserSettings settings = new UserSettings();
        settings.setSettingKey(dto.getSettingKey());
        settings.setSettingValue(dto.getSettingValue());

        return settings;
    }

    @Override
    protected UserSettings mapDtoToEntity(UserSettingsResponse dto, UserSettings entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getSettingValue() != null) {
            entity.setSettingValue(dto.getSettingValue());
        }
        // SettingKey typically not updated

        return entity;
    }

    /**
     * Creates a new UserSettings from an upsert request.
     *
     * @param request The upsert request
     * @param user    The user
     * @return A new UserSettings entity
     */
    public UserSettings createFromRequest(UserSettingsUpsertRequest request, User user) {

        if (request == null || user == null) {
            return null;
        }

        return UserSettings
            .builder()
            .user(user)
            .settingKey(request.getSettingKey())
            .settingValue(request.getSettingValue())
            .build();
    }

    /**
     * Updates UserSettings from an upsert request.
     *
     * @param request  The upsert request
     * @param settings The settings to update
     * @return The updated UserSettings
     */
    public UserSettings updateFromRequest(UserSettingsUpsertRequest request, UserSettings settings) {

        if (request == null || settings == null) {
            return settings;
        }

        settings.setSettingValue(request.getSettingValue());
        return settings;
    }
}