package com.kardio.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.kardio.dto.classroom.ClassCreateRequest;
import com.kardio.dto.classroom.ClassDetailedResponse;
import com.kardio.dto.classroom.ClassMemberResponse;
import com.kardio.dto.classroom.ClassModuleResponse;
import com.kardio.dto.classroom.ClassResponse;
import com.kardio.dto.classroom.ClassSummaryResponse;
import com.kardio.dto.classroom.ClassUpdateRequest;
import com.kardio.entity.Class;
import com.kardio.entity.User;
import com.kardio.entity.enums.MemberRole;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for Class entity.
 * Handles mapping between Class entities and various related DTOs.
 */
@Component
@RequiredArgsConstructor
public class ClassMapper extends AbstractGenericMapper<Class, ClassResponse> {

    private final UserMapper userMapper;

    @Override
    protected ClassResponse mapToDto(Class entity) {
        if (entity == null) {
            return null;
        }

        return ClassResponse
            .builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .creator(userMapper.toDto(entity.getCreator()))
            .createdAt(entity.getCreatedAt())
            .memberCount(0) // This needs to be set separately
            .moduleCount(0) // This needs to be set separately
            .build();
    }

    @Override
    protected Class mapToEntity(ClassResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // Creator would need to be set separately
        Class classEntity = new Class();
        classEntity.setName(dto.getName());
        classEntity.setDescription(dto.getDescription());

        return classEntity;
    }

    @Override
    protected Class mapDtoToEntity(ClassResponse dto, Class entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        // Creator typically not updated

        return entity;
    }

    /**
     * Creates a new Class from a create request.
     *
     * @param request The create request
     * @param creator The user creating the class
     * @return A new Class entity
     */
    public Class createFromRequest(ClassCreateRequest request, User creator) {

        if (request == null || creator == null) {
            return null;
        }

        return Class.builder().name(request.getName()).description(request.getDescription()).creator(creator).build();
    }

    /**
     * Updates a Class from an update request.
     *
     * @param request     The update request
     * @param classEntity The class to update
     * @return The updated Class
     */
    public Class updateFromRequest(ClassUpdateRequest request, Class classEntity) {

        if (request == null || classEntity == null) {
            return classEntity;
        }

        if (request.getName() != null) {
            classEntity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            classEntity.setDescription(request.getDescription());
        }

        return classEntity;
    }

    /**
     * Maps a Class to a summary response for list views.
     *
     * @param classEntity The class to map
     * @param memberCount Count of members in the class
     * @param userRole    The role of the current user in this class
     * @return A class summary response
     */
    public ClassSummaryResponse toSummaryResponse(Class classEntity, Integer memberCount, MemberRole userRole) {

        if (classEntity == null) {
            return null;
        }

        return ClassSummaryResponse
            .builder()
            .id(classEntity.getId())
            .name(classEntity.getName())
            .creator(userMapper.toDto(classEntity.getCreator()))
            .memberCount(memberCount)
            .userRole(userRole)
            .build();
    }

    /**
     * Maps a Class to a detailed response with members and modules.
     *
     * @param classEntity The class to map
     * @param memberCount Count of members in the class
     * @param moduleCount Count of modules in the class
     * @param members     List of class member responses
     * @param modules     List of class module responses
     * @return A detailed class response
     */
    public ClassDetailedResponse toDetailedResponse(
            Class classEntity,
            Integer memberCount,
            Integer moduleCount,
            List<ClassMemberResponse> members,
            List<ClassModuleResponse> modules) {

        if (classEntity == null) {
            return null;
        }

        return ClassDetailedResponse
            .builder()
            .id(classEntity.getId())
            .name(classEntity.getName())
            .description(classEntity.getDescription())
            .creator(userMapper.toDto(classEntity.getCreator()))
            .createdAt(classEntity.getCreatedAt())
            .updatedAt(classEntity.getUpdatedAt())
            .memberCount(memberCount)
            .moduleCount(moduleCount)
            .members(members != null ? members : Collections.emptyList())
            .modules(modules != null ? modules : Collections.emptyList())
            .build();
    }
}