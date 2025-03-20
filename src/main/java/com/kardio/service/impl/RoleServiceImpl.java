package com.kardio.service.impl;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kardio.dto.role.RoleCreateRequest;
import com.kardio.dto.role.RoleResponse;
import com.kardio.dto.role.RoleUpdateRequest;
import com.kardio.entity.Role;
import com.kardio.entity.enums.RoleType;
import com.kardio.exception.KardioException;
import com.kardio.mapper.RoleMapper;
import com.kardio.repository.RoleRepository;
import com.kardio.service.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RoleService for role management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        log.debug("Getting role by ID: {}", id);

        final Role role = findRoleById(id);
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) {
        log.debug("Getting role by name: {}", name);

        final Role role = roleRepository.findByName(name).orElseThrow(() -> {
            log.error("Role not found with name: {}", name);
            return new KardioException(messageSource.getMessage("error.resource.notfound", new Object[]{
                    messageSource.getMessage("entity.role", null, LocaleContextHolder.getLocale()), name
            }, LocaleContextHolder.getLocale()), org.springframework.http.HttpStatus.NOT_FOUND);
        });

        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleByType(RoleType roleType) {
        log.debug("Getting role by type: {}", roleType);
        return getRoleByName(roleType.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.debug("Getting all roles");

        final List<Role> roles = roleRepository.findAll();
        return roleMapper.toDtoList(roles);
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        log.info("Creating role with name: {}", request.getName());

        // Check if role with same name already exists
        if (roleRepository.existsByName(request.getName())) {
            log.error("Role already exists with name: {}", request.getName());
            throw new KardioException(messageSource.getMessage("error.resource.alreadyexists", new Object[]{
                    messageSource.getMessage("entity.role", null, LocaleContextHolder.getLocale()), "name",
                    request.getName()
            }, LocaleContextHolder.getLocale()), org.springframework.http.HttpStatus.CONFLICT);
        }

        final Role role = roleMapper.createFromRequest(request);
        final Role savedRole = roleRepository.save(role);

        log.info("Role created successfully with ID: {}", savedRole.getId());
        return roleMapper.toDto(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleUpdateRequest request) {
        log.info("Updating role with ID: {}", id);

        final Role role = findRoleById(id);

        // Don't allow updating built-in roles
        checkIfBuiltInRole(role);

        final Role updatedRole = roleMapper.updateFromRequest(request, role);
        final Role savedRole = roleRepository.save(updatedRole);

        log.info("Role updated successfully: {}", savedRole.getId());
        return roleMapper.toDto(savedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        log.info("Deleting role with ID: {}", id);

        final Role role = findRoleById(id);

        // Don't allow deleting built-in roles
        checkIfBuiltInRole(role);

        // Check if role is assigned to any users
        if (!role.getUsers().isEmpty()) {
            log.error("Cannot delete role {} as it is assigned to {} users", role.getName(), role.getUsers().size());
            throw new KardioException(
                messageSource
                    .getMessage(
                        "error.role.inuse",
                        null,
                        "Cannot delete role as it is assigned to users",
                        LocaleContextHolder.getLocale()),
                org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", id);
    }

    /**
     * Finds a role by ID or throws an exception.
     *
     * @param id Role ID
     * @return Role entity
     * @throws KardioException if role not found
     */
    private Role findRoleById(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> {
            log.error("Role not found with ID: {}", id);
            return new KardioException(messageSource.getMessage("error.resource.notfound", new Object[]{
                    messageSource.getMessage("entity.role", null, LocaleContextHolder.getLocale()), id
            }, LocaleContextHolder.getLocale()), org.springframework.http.HttpStatus.NOT_FOUND);
        });
    }

    /**
     * Checks if a role is a built-in role and throws an exception if it is.
     *
     * @param role Role to check
     * @throws KardioException if role is built-in
     */
    private void checkIfBuiltInRole(Role role) {
        // Check for built-in roles that shouldn't be modified
        for (RoleType builtInRole : new RoleType[]{
                RoleType.ADMIN, RoleType.USER
        }) {
            if (builtInRole.getValue().equals(role.getName())) {
                log.error("Cannot modify built-in role: {}", role.getName());
                throw new KardioException(
                    messageSource
                        .getMessage(
                            "error.role.builtin",
                            null,
                            "Cannot modify built-in role",
                            LocaleContextHolder.getLocale()),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
            }
        }
    }
}