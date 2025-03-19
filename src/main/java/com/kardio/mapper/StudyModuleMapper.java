package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.folder.FolderResponse;
import com.kardio.dto.module.StudyModuleCreateRequest;
import com.kardio.dto.module.StudyModuleDetailedResponse;
import com.kardio.dto.module.StudyModuleResponse;
import com.kardio.dto.module.StudyModuleSummaryResponse;
import com.kardio.dto.module.StudyModuleUpdateRequest;
import com.kardio.entity.Folder;
import com.kardio.entity.StudyModule;
import com.kardio.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for StudyModule entity.
 * Handles mapping between StudyModule entities and various related DTOs.
 */
@Component
@RequiredArgsConstructor
public class StudyModuleMapper extends AbstractGenericMapper<StudyModule, StudyModuleResponse> {

    private final UserMapper userMapper;
    private final FolderMapper folderMapper;

    @Override
    protected StudyModuleResponse mapToDto(StudyModule entity) {
        if (entity == null) {
            return null;
        }

        StudyModuleResponse response = StudyModuleResponse
            .builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .visibility(entity.getVisibility())
            .lastStudiedAt(entity.getLastStudiedAt())
            .createdAt(entity.getCreatedAt())
            .creator(userMapper.toDto(entity.getCreator()))
            .vocabularyCount(0) // This needs to be set separately
            .build();

        if (entity.getFolder() != null) {
            response.setFolder(folderMapper.toDto(entity.getFolder()));
        }

        return response;
    }

    @Override
    protected StudyModule mapToEntity(StudyModuleResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // In real usage, Creator and Folder would need to be set separately
        StudyModule module = new StudyModule();
        module.setName(dto.getName());
        module.setDescription(dto.getDescription());
        module.setVisibility(dto.getVisibility());
        module.setLastStudiedAt(dto.getLastStudiedAt());

        return module;
    }

    @Override
    protected StudyModule mapDtoToEntity(StudyModuleResponse dto, StudyModule entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getVisibility() != null) {
            entity.setVisibility(dto.getVisibility());
        }
        if (dto.getLastStudiedAt() != null) {
            entity.setLastStudiedAt(dto.getLastStudiedAt());
        }
        // Folder and creator typically not updated this way

        return entity;
    }

    /**
     * Maps StudyModule to a detailed response with additional statistics.
     *
     * @param module               The study module to map
     * @param vocabularyCount      Count of vocabularies in the module
     * @param averageAccuracy      Average accuracy rate across the module
     * @param completionPercentage Percentage of completed vocabularies
     * @return A detailed response DTO
     */
    public StudyModuleDetailedResponse toDetailedResponse(
            StudyModule module,
            Integer vocabularyCount,
            Double averageAccuracy,
            Double completionPercentage) {

        if (module == null) {
            return null;
        }

        FolderResponse folderResponse = null;
        if (module.getFolder() != null) {
            folderResponse = folderMapper.toDto(module.getFolder());
        }

        return StudyModuleDetailedResponse
            .builder()
            .id(module.getId())
            .name(module.getName())
            .description(module.getDescription())
            .visibility(module.getVisibility())
            .lastStudiedAt(module.getLastStudiedAt())
            .createdAt(module.getCreatedAt())
            .updatedAt(module.getUpdatedAt())
            .deletedAt(module.getDeletedAt())
            .creator(userMapper.toDto(module.getCreator()))
            .folder(folderResponse)
            .vocabularyCount(vocabularyCount)
            .averageAccuracy(averageAccuracy)
            .completionPercentage(completionPercentage)
            .build();
    }

    /**
     * Maps StudyModule to a summary response.
     *
     * @param module          The study module to map
     * @param vocabularyCount Count of vocabularies in the module
     * @return A summary response DTO
     */
    public StudyModuleSummaryResponse toSummaryResponse(StudyModule module, Integer vocabularyCount) {
        if (module == null) {
            return null;
        }

        return StudyModuleSummaryResponse
            .builder()
            .id(module.getId())
            .name(module.getName())
            .visibility(module.getVisibility())
            .vocabularyCount(vocabularyCount)
            .creator(userMapper.toDto(module.getCreator()))
            .build();
    }

    /**
     * Creates a new StudyModule from a create request.
     *
     * @param request The create request
     * @param creator The user creating the module
     * @param folder  The folder to place the module in (can be null)
     * @return A new StudyModule entity
     */
    public StudyModule createFromRequest(StudyModuleCreateRequest request, User creator, Folder folder) {

        if (request == null || creator == null) {
            return null;
        }

        return StudyModule
            .builder()
            .name(request.getName())
            .description(request.getDescription())
            .visibility(request.getVisibility())
            .creator(creator)
            .folder(folder)
            .build();
    }

    /**
     * Updates a StudyModule from an update request.
     *
     * @param request The update request
     * @param module  The module to update
     * @param folder  The new folder (can be null to keep current)
     * @return The updated StudyModule
     */
    public StudyModule updateFromRequest(StudyModuleUpdateRequest request, StudyModule module, Folder folder) {

        if (request == null || module == null) {
            return module;
        }

        if (request.getName() != null) {
            module.setName(request.getName());
        }
        if (request.getDescription() != null) {
            module.setDescription(request.getDescription());
        }
        if (request.getVisibility() != null) {
            module.setVisibility(request.getVisibility());
        }
        if (folder != null) {
            module.setFolder(folder);
        }

        return module;
    }
}