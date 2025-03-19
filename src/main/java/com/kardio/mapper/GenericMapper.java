package com.kardio.mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Generic mapper interface for converting between different object types.
 * This provides standard mapping methods to convert entities to DTOs and vice
 * versa.
 *
 * @param <E> Entity type
 * @param <D> DTO type
 */
public interface GenericMapper<E, D> {

    /**
     * Maps an entity to a DTO.
     *
     * @param entity Entity to be mapped
     * @return Mapped DTO object
     */
    D toDto(E entity);

    /**
     * Maps a DTO to an entity.
     *
     * @param dto DTO to be mapped
     * @return Mapped entity object
     */
    E toEntity(D dto);

    /**
     * Maps a collection of entities to a list of DTOs.
     *
     * @param entities Collection of entities to be mapped
     * @return List of mapped DTO objects
     */
    default List<D> toDtoList(Collection<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Maps a collection of DTOs to a list of entities.
     *
     * @param dtos Collection of DTOs to be mapped
     * @return List of mapped entity objects
     */
    default List<E> toEntityList(Collection<D> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    /**
     * Maps a page of entities to a page of DTOs.
     *
     * @param page Page of entities to be mapped
     * @return Page of mapped DTO objects
     */
    default Page<D> toDtoPage(Page<E> page) {
        if (page == null) {
            return Page.empty();
        }
        List<D> dtos = toDtoList(page.getContent());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    /**
     * Updates an entity from a DTO.
     * This method can be overridden to provide custom update logic.
     *
     * @param entity Entity to be updated
     * @param dto    DTO containing update data
     * @return Updated entity
     */
    default E updateEntityFromDto(D dto, E entity) {
        // Default implementation does nothing
        // Override in concrete mapper implementations
        return entity;
    }
}