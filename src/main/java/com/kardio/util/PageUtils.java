package com.kardio.util;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.kardio.dto.common.PageResponse;

/**
 * Utility class for handling pagination operations.
 * Provides methods to convert Spring Data Page objects to PageResponse DTOs.
 */
@Component
public class PageUtils {

    private PageUtils() {

    }

    /**
     * Creates a PageResponse from a Spring Data Page and a mapping function.
     *
     * @param <T>    The entity type
     * @param <R>    The DTO type
     * @param page   The Spring Data Page object
     * @param mapper The function to map from entity to DTO
     * @return A PageResponse containing the mapped DTOs
     */
    public static <T, R> PageResponse<R> createPageResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream().map(mapper).toList();

        return PageResponse
            .<R>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
    }

    /**
     * Creates a PageResponse directly from a Spring Data Page of DTOs.
     *
     * @param <R>      The DTO type
     * @param page     The Spring Data Page object already containing DTOs
     * @param pageable The Pageable object used for the query
     * @return A PageResponse containing the DTOs
     */
    public static <R> PageResponse<R> createPageResponse(Page<R> page, Pageable pageable) {
        return PageResponse
            .<R>builder()
            .content(page.getContent())
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
    }

    /**
     * Creates an empty PageResponse.
     *
     * @param <R>      The DTO type
     * @param pageable The Pageable object used for the query
     * @return An empty PageResponse
     */
    public static <R> PageResponse<R> emptyPageResponse(Pageable pageable) {
        return PageResponse
            .<R>builder()
            .content(List.of())
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .build();
    }
}