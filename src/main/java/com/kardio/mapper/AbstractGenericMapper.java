package com.kardio.mapper;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

/**
 * Abstract implementation of GenericMapper that provides a base for
 * entity-to-DTO mapping.
 * Implements common functionality and provides a template for concrete mappers.
 *
 * @param <E> Entity type
 * @param <D> DTO type
 */
public abstract class AbstractGenericMapper<E, D> implements GenericMapper<E, D> {

    protected final Class<E> entityClass;
    protected final Class<D> dtoClass;

    /**
     * Constructor that initializes entity and DTO class types using reflection.
     */
    @SuppressWarnings("unchecked")
    protected AbstractGenericMapper() {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[0];
        this.dtoClass = (Class<D>) genericSuperclass.getActualTypeArguments()[1];
    }

    /**
     * Safely maps an entity to a DTO, handling null values.
     *
     * @param entity Entity to be mapped
     * @return Mapped DTO or null if the entity is null
     */
    @Override
    public D toDto(E entity) {
        return entity != null ? mapToDto(entity) : null;
    }

    /**
     * Safely maps a DTO to an entity, handling null values.
     *
     * @param dto DTO to be mapped
     * @return Mapped entity or null if the DTO is null
     */
    @Override
    public E toEntity(D dto) {
        return dto != null ? mapToEntity(dto) : null;
    }

    /**
     * Maps non-null fields from DTO to entity for updating purposes.
     * This method should be overridden by implementations to handle specific
     * mapping.
     *
     * @param dto    Source DTO with updated values
     * @param entity Target entity to update
     * @return Updated entity
     */
    @Override
    public E updateEntityFromDto(D dto, E entity) {
        Objects.requireNonNull(dto, "DTO cannot be null for entity update");
        Objects.requireNonNull(entity, "Entity cannot be null for update");
        return mapDtoToEntity(dto, entity);
    }

    /**
     * Template method for implementing concrete mapping from entity to DTO.
     *
     * @param entity Entity to map
     * @return Mapped DTO
     */
    protected abstract D mapToDto(E entity);

    /**
     * Template method for implementing concrete mapping from DTO to entity.
     *
     * @param dto DTO to map
     * @return Mapped entity
     */
    protected abstract E mapToEntity(D dto);

    /**
     * Template method for implementing concrete partial update from DTO to existing
     * entity.
     *
     * @param dto    Source DTO with updated values
     * @param entity Target entity to update
     * @return Updated entity
     */
    protected abstract E mapDtoToEntity(D dto, E entity);
}