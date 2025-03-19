package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.streak.StreakResponse;
import com.kardio.entity.Streak;
import com.kardio.entity.User;

/**
 * Mapper for Streak entity.
 * Handles mapping between Streak entities and related DTOs.
 */
@Component
public class StreakMapper extends AbstractGenericMapper<Streak, StreakResponse> {

    @Override
    protected StreakResponse mapToDto(Streak entity) {
        if (entity == null) {
            return null;
        }

        return StreakResponse
            .builder()
            .id(entity.getId())
            .currentStreak(entity.getCurrentStreak())
            .longestStreak(entity.getLongestStreak())
            .lastActivityDate(entity.getLastActivityDate())
            .isActiveToday(entity.isActiveToday())
            .build();
    }

    @Override
    protected Streak mapToEntity(StreakResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // User would need to be set separately
        Streak streak = new Streak();
        streak.setCurrentStreak(dto.getCurrentStreak());
        streak.setLongestStreak(dto.getLongestStreak());
        streak.setLastActivityDate(dto.getLastActivityDate());

        return streak;
    }

    @Override
    protected Streak mapDtoToEntity(StreakResponse dto, Streak entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getCurrentStreak() != null) {
            entity.setCurrentStreak(dto.getCurrentStreak());
        }
        if (dto.getLongestStreak() != null) {
            entity.setLongestStreak(dto.getLongestStreak());
        }
        if (dto.getLastActivityDate() != null) {
            entity.setLastActivityDate(dto.getLastActivityDate());
        }

        return entity;
    }

    /**
     * Creates a new Streak for a user.
     *
     * @param user The user
     * @return A new Streak entity
     */
    public Streak createInitialStreak(User user) {
        if (user == null) {
            return null;
        }

        return Streak.builder().user(user).currentStreak(0).longestStreak(0).build();
    }
}