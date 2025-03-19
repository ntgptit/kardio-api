package com.kardio.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.kardio.dto.folder.FolderCreateRequest;
import com.kardio.dto.folder.FolderDetailedResponse;
import com.kardio.dto.folder.FolderHierarchyResponse;
import com.kardio.dto.folder.FolderResponse;
import com.kardio.dto.folder.FolderUpdateRequest;
import com.kardio.dto.module.StudyModuleSummaryResponse;
import com.kardio.entity.Folder;
import com.kardio.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for Folder entity.
 * Handles mapping between Folder entities and various related DTOs.
 */
@Component
@RequiredArgsConstructor
public class FolderMapper extends AbstractGenericMapper<Folder, FolderResponse> {

    private final UserMapper userMapper;

    @Override
    protected FolderResponse mapToDto(Folder entity) {
        if (entity == null) {
            return null;
        }

        FolderResponse response = FolderResponse
            .builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .build();

        if (entity.getParentFolder() != null) {
            response.setParentFolderId(entity.getParentFolder().getId());
            response.setParentFolderName(entity.getParentFolder().getName());
        }

        return response;
    }

    @Override
    protected Folder mapToEntity(FolderResponse dto) {
        if (dto == null) {
            return null;
        }

        // Note: This is a simplified implementation
        // In real usage, User and Parent Folder would need to be set separately
        Folder folder = new Folder();
        folder.setName(dto.getName());
        folder.setDescription(dto.getDescription());

        return folder;
    }

    @Override
    protected Folder mapDtoToEntity(FolderResponse dto, Folder entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        // Parent folder typically updated separately

        return entity;
    }

    /**
     * Creates a new Folder from a create request.
     *
     * @param request      The create request
     * @param user         The owner of the folder
     * @param parentFolder The parent folder (can be null for root folder)
     * @return A new Folder entity
     */
    public Folder createFromRequest(FolderCreateRequest request, User user, Folder parentFolder) {

        if (request == null || user == null) {
            return null;
        }

        return Folder
            .builder()
            .user(user)
            .name(request.getName())
            .description(request.getDescription())
            .parentFolder(parentFolder)
            .build();
    }

    /**
     * Updates a Folder from an update request.
     *
     * @param request      The update request
     * @param folder       The folder to update
     * @param parentFolder The new parent folder (can be null to keep current)
     * @return The updated Folder
     */
    public Folder updateFromRequest(FolderUpdateRequest request, Folder folder, Folder parentFolder) {

        if (request == null || folder == null) {
            return folder;
        }

        if (request.getName() != null) {
            folder.setName(request.getName());
        }
        if (request.getDescription() != null) {
            folder.setDescription(request.getDescription());
        }
        if (parentFolder != null) {
            folder.setParentFolder(parentFolder);
        }

        return folder;
    }

    /**
     * Maps a Folder to a detailed response with subfolder and module information.
     *
     * @param folder     The folder to map
     * @param subfolders List of subfolder responses
     * @param modules    List of module summary responses in this folder
     * @return A detailed folder response
     */
    public FolderDetailedResponse toDetailedResponse(
            Folder folder,
            List<FolderResponse> subfolders,
            List<StudyModuleSummaryResponse> modules) {

        if (folder == null) {
            return null;
        }

        FolderDetailedResponse response = FolderDetailedResponse
            .builder()
            .id(folder.getId())
            .name(folder.getName())
            .description(folder.getDescription())
            .createdAt(folder.getCreatedAt())
            .updatedAt(folder.getUpdatedAt())
            .user(userMapper.toDto(folder.getUser()))
            .subfolders(subfolders != null ? subfolders : Collections.emptyList())
            .modules(modules != null ? modules : Collections.emptyList())
            .build();

        if (folder.getParentFolder() != null) {
            response.setParentFolderId(folder.getParentFolder().getId());
            response.setParentFolderName(folder.getParentFolder().getName());
        }

        return response;
    }

    /**
     * Maps a Folder to a hierarchy response for tree structure.
     *
     * @param folder      The folder to map
     * @param children    Child folders in the hierarchy
     * @param moduleCount Count of modules in this folder
     * @return A folder hierarchy response
     */
    public
            FolderHierarchyResponse
            toHierarchyResponse(Folder folder, List<FolderHierarchyResponse> children, Integer moduleCount) {

        if (folder == null) {
            return null;
        }

        FolderHierarchyResponse response = FolderHierarchyResponse
            .builder()
            .id(folder.getId())
            .name(folder.getName())
            .children(children != null ? children : Collections.emptyList())
            .moduleCount(moduleCount)
            .build();

        if (folder.getParentFolder() != null) {
            response.setParentFolderId(folder.getParentFolder().getId());
        }

        return response;
    }

    /**
     * Recursively builds a folder hierarchy from a list of folders.
     *
     * @param allFolders     Complete list of folders
     * @param parentId       Parent folder ID (null for root folders)
     * @param moduleCountMap Map of folder IDs to module counts
     * @return List of folder hierarchy responses
     */
    public
            List<FolderHierarchyResponse>
            buildFolderHierarchy(List<Folder> allFolders, UUID parentId, Map<UUID, Integer> moduleCountMap) {

        if (allFolders == null || allFolders.isEmpty()) {
            return Collections.emptyList();
        }

        return allFolders.stream().filter(folder -> {
            if (parentId == null) {
                return folder.getParentFolder() == null;
            } else {
                return folder.getParentFolder() != null && folder.getParentFolder().getId().equals(parentId);
            }
        }).map(folder -> {
            Integer moduleCount = moduleCountMap.getOrDefault(folder.getId(), 0);
            List<FolderHierarchyResponse> children = buildFolderHierarchy(allFolders, folder.getId(), moduleCountMap);
            return toHierarchyResponse(folder, children, moduleCount);
        }).collect(Collectors.toList());
    }
}