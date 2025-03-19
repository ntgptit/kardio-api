package com.kardio.config.mapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for all mappers in the application.
 * Centralizes mapper configuration and provides utility beans.
 */
@Configuration
public class MapperConfig {

    /**
     * Creates a map of UUID to Integer for module counts.
     * This utility bean helps with processing folders and their module counts.
     *
     * @return A function that creates a map from (UUID, Integer) pairs
     */
    @Bean
    public Function<List<Object[]>, Map<UUID, Integer>> moduleCountMapFunction() {
        return results -> results
            .stream()
            .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).intValue(), (a, b) -> a));
    }
}