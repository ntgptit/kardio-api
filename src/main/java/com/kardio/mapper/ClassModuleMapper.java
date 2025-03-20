package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.classroom.ClassModuleResponse;
import com.kardio.entity.Class;
import com.kardio.entity.ClassModule;
import com.kardio.entity.StudyModule;
import com.kardio.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for ClassModule entity.
 * Handles mapping between ClassModule entities and related DTOs.
 */
@Component
@RequiredArgsConstructor
public class ClassModuleMapper extends AbstractGenericMapper<ClassModule, ClassModuleResponse> {

    private final UserMapper userMapper;
    private final StudyModuleMapper studyModuleMapper;

    @Override
    protected ClassModuleResponse mapToDto(ClassModule entity) {
        if (entity == null) {
            return null;
        }

        return ClassModuleResponse
            .builder()
            .id(entity.getId())
            .classId(entity.getClassEntity().getId())
            .module(studyModuleMapper.toDto(entity.getModule()))
            .addedBy(entity.getAddedBy() != null ? userMapper.toDto(entity.getAddedBy()) : null)
            .createdAt(entity.getCreatedAt())
            .build();
    }

    @Override
    protected ClassModule mapToEntity(ClassModuleResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // Class, Module, and AddedBy would need to be set separately
        return new ClassModule();
    }

    @Override
    protected ClassModule mapDtoToEntity(ClassModuleResponse dto, ClassModule entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        // No properties to update directly from DTO
        return entity;
    }

    /**
     * Creates a new ClassModule for a class and module.
     *
     * @param classEntity The class
     * @param module      The study module
     * @param addedBy     The user adding the module
     * @return A new ClassModule entity
     */
    public ClassModule createClassModule(Class classEntity, StudyModule module, User addedBy) {

        if (classEntity == null || module == null) {
            return null;
        }

        return ClassModule.builder().classEntity(classEntity).module(module).addedBy(addedBy).build();
    }
}