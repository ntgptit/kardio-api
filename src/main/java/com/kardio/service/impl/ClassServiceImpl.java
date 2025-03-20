package com.kardio.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.constant.AppConstants.EntityNames;
import com.kardio.constant.AppConstants.ErrorMessages;
import com.kardio.dto.classroom.ClassCreateRequest;
import com.kardio.dto.classroom.ClassDetailedResponse;
import com.kardio.dto.classroom.ClassMemberResponse;
import com.kardio.dto.classroom.ClassModuleResponse;
import com.kardio.dto.classroom.ClassResponse;
import com.kardio.dto.classroom.ClassUpdateRequest;
import com.kardio.dto.common.PageResponse;
import com.kardio.dto.common.SuccessResponse;
import com.kardio.entity.Class;
import com.kardio.entity.ClassMember;
import com.kardio.entity.User;
import com.kardio.entity.enums.MemberRole;
import com.kardio.exception.KardioException;
import com.kardio.mapper.ClassMapper;
import com.kardio.mapper.ClassMemberMapper;
import com.kardio.mapper.ClassModuleMapper;
import com.kardio.repository.ClassMemberRepository;
import com.kardio.repository.ClassModuleRepository;
import com.kardio.repository.ClassRepository;
import com.kardio.repository.UserRepository;
import com.kardio.service.ClassService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ClassService for class management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final ClassMemberRepository classMemberRepository;
    private final ClassModuleRepository classModuleRepository;
    private final UserRepository userRepository;
    private final ClassMapper classMapper;
    private final ClassMemberMapper classMemberMapper;
    private final ClassModuleMapper classModuleMapper;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public ClassResponse createClass(ClassCreateRequest request, UUID creatorId) {
        log.info("Creating class with name: {} for user ID: {}", request.getName(), creatorId);

        // Validate and get creator
        final User creator = userRepository.findById(creatorId).orElseThrow(() -> {
            log.error("User not found with ID: {}", creatorId);
            return KardioException.resourceNotFound(messageSource, EntityNames.USER, creatorId);
        });

        // Create class entity
        final Class classEntity = classMapper.createFromRequest(request, creator);
        final Class savedClass = classRepository.save(classEntity);

        // Create creator as admin member
        final ClassMember adminMember = classMemberMapper.createMember(savedClass, creator, MemberRole.ADMIN);
        classMemberRepository.save(adminMember);

        log.info("Class created successfully with ID: {}", savedClass.getId());
        return classMapper.toDto(savedClass);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassResponse getClassById(UUID id, UUID userId) {
        log.debug("Getting class by ID: {} for user ID: {}", id, userId);

        final Class classEntity = findClassById(id);

        // Check if user can access the class
        if (!canUserAccessClass(classEntity, userId)) {
            log.error("User {} does not have access to class {}", userId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    ErrorMessages.FORBIDDEN_RESOURCE,
                    messageSource.getMessage(EntityNames.CLASS, null, LocaleContextHolder.getLocale()));
        }

        // Get member count and module count for efficient display
        final int memberCount = (int) classMemberRepository.countByClassEntityId(id);
        final int moduleCount = (int) classModuleRepository.countByClassEntityId(id);

        final ClassResponse response = classMapper.toDto(classEntity);
        response.setMemberCount(memberCount);
        response.setModuleCount(moduleCount);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ClassDetailedResponse getClassDetailedById(UUID id, UUID userId) {
        log.debug("Getting detailed class by ID: {} for user ID: {}", id, userId);

        final Class classEntity = findClassById(id);

        // Check if user can access the class
        if (!canUserAccessClass(classEntity, userId)) {
            log.error("User {} does not have access to class {}", userId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    ErrorMessages.FORBIDDEN_RESOURCE,
                    messageSource.getMessage(EntityNames.CLASS, null, LocaleContextHolder.getLocale()));
        }

        // Get member count and module count
        final int memberCount = (int) classMemberRepository.countByClassEntityId(id);
        final int moduleCount = (int) classModuleRepository.countByClassEntityId(id);

        // Get members
        final List<ClassMember> members = classMemberRepository.findByClassEntityId(id);
        final List<ClassMemberResponse> memberResponses = classMemberMapper.toDtoList(members);

        // Get modules
        final List<ClassModuleResponse> moduleResponses = classModuleMapper
            .toDtoList(classModuleRepository.findByClassEntityId(id));

        return classMapper.toDetailedResponse(classEntity, memberCount, moduleCount, memberResponses, moduleResponses);
    }

    @Override
    @Transactional
    public ClassResponse updateClass(UUID id, ClassUpdateRequest request, UUID userId) {
        log.info("Updating class with ID: {} by user ID: {}", id, userId);

        final Class classEntity = findClassById(id);

        // Check if user can modify the class
        if (!canModifyClass(classEntity.getId(), userId)) {
            log.error("User {} does not have permission to update class {}", userId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    ErrorMessages.FORBIDDEN_OWNER,
                    "update",
                    messageSource.getMessage(EntityNames.CLASS, null, LocaleContextHolder.getLocale()));
        }

        // Update class
        final Class updatedClass = classMapper.updateFromRequest(request, classEntity);
        final Class savedClass = classRepository.save(updatedClass);

        // Get updated counts
        final int memberCount = (int) classMemberRepository.countByClassEntityId(id);
        final int moduleCount = (int) classModuleRepository.countByClassEntityId(id);

        final ClassResponse response = classMapper.toDto(savedClass);
        response.setMemberCount(memberCount);
        response.setModuleCount(moduleCount);

        log.info("Class updated successfully: {}", savedClass.getId());
        return response;
    }

    @Override
    @Transactional
    public SuccessResponse deleteClass(UUID id, UUID userId) {
        log.info("Deleting class with ID: {} by user ID: {}", id, userId);

        final Class classEntity = findClassById(id);

        // Check if user is the owner
        if (!classEntity.getCreator().getId().equals(userId)) {
            log.error("User {} is not the owner of class {}", userId, id);
            throw KardioException
                .forbidden(
                    messageSource,
                    ErrorMessages.FORBIDDEN_OWNER,
                    "delete",
                    messageSource.getMessage(EntityNames.CLASS, null, LocaleContextHolder.getLocale()));
        }

        // Soft delete
        classEntity.softDelete();
        classRepository.save(classEntity);

        log.info("Class deleted successfully: {}", id);
        return SuccessResponse.of(messageSource.getMessage("success.deleted", new Object[]{
                messageSource.getMessage(EntityNames.CLASS, null, LocaleContextHolder.getLocale())
        }, LocaleContextHolder.getLocale()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassResponse> getClassesByCreator(UUID userId, Pageable pageable) {
        log.debug("Getting classes by creator ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound(messageSource, EntityNames.USER, userId);
        }

        final Page<Class> classPage = classRepository.findByCreatorId(userId, pageable);

        return createClassResponsePage(classPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassResponse> getClassesByMember(UUID userId, Pageable pageable) {
        log.debug("Getting classes where user ID: {} is a member", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw KardioException.resourceNotFound(messageSource, EntityNames.USER, userId);
        }

        final Page<Class> classPage = classRepository.findByMemberId(userId, pageable);

        return createClassResponsePage(classPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassResponse> searchClasses(String term, Pageable pageable) {
        log.debug("Searching classes with term: {}", term);

        // Validate search term
        if (StringUtils.isEmpty(term) || term.length() < 2) {
            throw KardioException.validationError(messageSource, "error.validation.searchterm", 2);
        }

        final Page<Class> classPage = classRepository.findByNameContainingIgnoreCase(term, pageable);

        return createClassResponsePage(classPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberRole getUserRole(UUID classId, UUID userId) {
        log.debug("Getting user role for class ID: {} and user ID: {}", classId, userId);

        final Optional<ClassMember> memberOpt = classMemberRepository.findByClassEntityIdAndUserId(classId, userId);
        return memberOpt.map(ClassMember::getRole).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canModifyClass(UUID classId, UUID userId) {
        log.debug("Checking if user ID: {} can modify class ID: {}", userId, classId);

        // Check if user is the creator
        final Class classEntity = findClassById(classId);
        if (classEntity.getCreator().getId().equals(userId)) {
            return true;
        }

        // Check if user has admin or teacher role
        final MemberRole role = getUserRole(classId, userId);
        return role != null && (role == MemberRole.ADMIN || role == MemberRole.TEACHER);
    }

    /**
     * Creates a paginated response of ClassResponse objects.
     *
     * @param classPage Page of Class entities
     * @param pageable  Pagination parameters
     * @return Paginated ClassResponse objects
     */
    private PageResponse<ClassResponse> createClassResponsePage(Page<Class> classPage, Pageable pageable) {
        if (classPage.isEmpty()) {
            return emptyPageResponse(pageable);
        }

        // Get class IDs for efficient batch counting
        final List<UUID> classIds = classPage.getContent().stream().map(Class::getId).toList();

        // Get member and module counts for all classes in batch queries
        final Map<UUID, Integer> memberCounts = getEntityCounts(classRepository.countMembersForClasses(classIds));

        final Map<UUID, Integer> moduleCounts = getEntityCounts(classRepository.countModulesForClasses(classIds));

        // Map to DTOs with counts
        final List<ClassResponse> responseDtos = classPage.getContent().stream().map(classEntity -> {
            final ClassResponse dto = classMapper.toDto(classEntity);
            dto.setMemberCount(memberCounts.getOrDefault(classEntity.getId(), 0));
            dto.setModuleCount(moduleCounts.getOrDefault(classEntity.getId(), 0));
            return dto;
        }).toList();

        return PageResponse
            .<ClassResponse>builder()
            .content(responseDtos)
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(classPage.getTotalElements())
            .totalPages(classPage.getTotalPages())
            .first(classPage.isFirst())
            .last(classPage.isLast())
            .build();
    }

    /**
     * Creates a map of entity ID to count from a query result.
     *
     * @param countData Query result containing [entityId, count]
     * @return Map of entity ID to count
     */
    private Map<UUID, Integer> getEntityCounts(List<Object[]> countData) {
        return countData
            .stream()
            .collect(
                Collectors
                    .toMap(
                        row -> (UUID) row[0],  // entityId
                        row -> ((Number) row[1]).intValue(),  // count
                        (a, b) -> a  // In case of duplicates, keep first
                    ));
    }

    /**
     * Creates an empty paginated response.
     *
     * @param pageable Pagination parameters
     * @return Empty paginated response
     */
    private <T> PageResponse<T> emptyPageResponse(Pageable pageable) {
        return PageResponse
            .<T>builder()
            .content(List.of())
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .build();
    }

    /**
     * Finds a class by ID or throws an exception.
     *
     * @param id Class ID
     * @return Class entity
     * @throws KardioException if class not found
     */
    private Class findClassById(UUID id) {
        return classRepository.findById(id).orElseThrow(() -> {
            log.error("Class not found with ID: {}", id);
            return KardioException.resourceNotFound(messageSource, EntityNames.CLASS, id);
        });
    }

    /**
     * Checks if a user can access a class.
     *
     * @param classEntity Class entity
     * @param userId      User ID
     * @return true if user can access the class
     */
    private boolean canUserAccessClass(Class classEntity, UUID userId) {
        // Creator always has access
        if (classEntity.getCreator().getId().equals(userId)) {
            return true;
        }

        // Check if user is a member
        return classMemberRepository.existsByClassEntityIdAndUserId(classEntity.getId(), userId);
    }
}