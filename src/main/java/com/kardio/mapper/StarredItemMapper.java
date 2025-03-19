package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.starred.StarredItemResponse;
import com.kardio.entity.StarredItem;
import com.kardio.entity.User;
import com.kardio.entity.Vocabulary;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for StarredItem entity.
 * Handles mapping between StarredItem entities and related DTOs.
 */
@Component
@RequiredArgsConstructor
public class StarredItemMapper extends AbstractGenericMapper<StarredItem, StarredItemResponse> {

    private final VocabularyMapper vocabularyMapper;

    @Override
    protected StarredItemResponse mapToDto(StarredItem entity) {
        if (entity == null) {
            return null;
        }

        return StarredItemResponse
            .builder()
            .id(entity.getId())
            .vocabulary(vocabularyMapper.toDto(entity.getVocabulary()))
            .createdAt(entity.getCreatedAt())
            .build();
    }

    @Override
    protected StarredItem mapToEntity(StarredItemResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // User and Vocabulary would need to be set separately
        StarredItem starredItem = new StarredItem();
        // No direct properties to set from DTO

        return starredItem;
    }

    @Override
    protected StarredItem mapDtoToEntity(StarredItemResponse dto, StarredItem entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        // No updateable properties in this entity
        return entity;
    }

    /**
     * Creates a new StarredItem for a user and vocabulary.
     *
     * @param user       The user
     * @param vocabulary The vocabulary to star
     * @return A new StarredItem entity
     */
    public StarredItem createStarredItem(User user, Vocabulary vocabulary) {

        if (user == null || vocabulary == null) {
            return null;
        }

        return StarredItem.builder().user(user).vocabulary(vocabulary).build();
    }
}