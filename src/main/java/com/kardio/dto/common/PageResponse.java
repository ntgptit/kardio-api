package com.kardio.dto.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paginated response wrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
	private List<T> content;
	private Integer page;
	private Integer size;
	private Long totalElements;
	private Integer totalPages;
	private Boolean first;
	private Boolean last;
}