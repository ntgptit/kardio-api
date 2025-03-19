package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.classroom.ClassMemberResponse;
import com.kardio.entity.Class;
import com.kardio.entity.ClassMember;
import com.kardio.entity.User;
import com.kardio.entity.enums.MemberRole;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for ClassMember entity.
 * Handles mapping between ClassMember entities and related DTOs.
 */
@Component
@RequiredArgsConstructor
public class ClassMemberMapper extends AbstractGenericMapper<ClassMember, ClassMemberResponse> {

    private final UserMapper userMapper;

    @Override
    protected ClassMemberResponse mapToDto(ClassMember entity) {
        if (entity == null) {
            return null;
        }

        return ClassMemberResponse
            .builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .classId(entity.getClassEntity().getId())
            .className(entity.getClassEntity().getName())
            .user(userMapper.toDto(entity.getUser()))
            .role(entity.getRole())
            .joinedAt(entity.getJoinedAt())
            .build();
    }

    @Override
    protected ClassMember mapToEntity(ClassMemberResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // User and Class would need to be set separately
        ClassMember member = new ClassMember();
        member.setRole(dto.getRole());

        return member;
    }

    @Override
    protected ClassMember mapDtoToEntity(ClassMemberResponse dto, ClassMember entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getRole() != null) {
            entity.setRole(dto.getRole());
        }
        // User and Class typically not updated

        return entity;
    }

    /**
     * Creates a new ClassMember for a class and user.
     *
     * @param classEntity The class
     * @param user        The user
     * @param role        The role of the user in the class
     * @return A new ClassMember entity
     */
    public ClassMember createMember(Class classEntity, User user, MemberRole role) {

        if (classEntity == null || user == null || role == null) {
            return null;
        }

        return ClassMember.builder().classEntity(classEntity).user(user).role(role).build();
    }
}