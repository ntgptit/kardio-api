package com.kardio.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kardio.entity.ClassModule;

@Repository
public interface ClassModuleRepository extends JpaRepository<ClassModule, UUID> {

    /**
     * Finds modules by class ID.
     *
     * @param classId Class ID
     * @return List of class modules
     */
    List<ClassModule> findByClassEntityId(UUID classId);

    /**
     * Finds modules by class ID with pagination.
     *
     * @param classId  Class ID
     * @param pageable Pagination information
     * @return Page of class modules
     */
    Page<ClassModule> findByClassEntityId(UUID classId, Pageable pageable);

    /**
     * Checks if a module exists in a class.
     *
     * @param classId  Class ID
     * @param moduleId Module ID
     * @return true if the module exists in the class, false otherwise
     */
    boolean existsByClassEntityIdAndModuleId(UUID classId, UUID moduleId);

    /**
     * Counts modules by class ID.
     *
     * @param classId Class ID
     * @return Count of modules
     */
    long countByClassEntityId(UUID classId);

    /**
     * Counts classes where a module is used.
     *
     * @param moduleId Module ID
     * @return Count of classes using the module
     */
    long countByModuleId(UUID moduleId);

    /**
     * Finds module IDs for a class.
     *
     * @param classId Class ID
     * @return List of module IDs
     */
    @Query("SELECT m.module.id FROM ClassModule m WHERE m.classEntity.id = :classId")
    List<UUID> findModuleIdsByClassId(@Param("classId") UUID classId);

    /**
     * Deletes a module from a class.
     *
     * @param classId  Class ID
     * @param moduleId Module ID
     */
    void deleteByClassEntityIdAndModuleId(UUID classId, UUID moduleId);
}