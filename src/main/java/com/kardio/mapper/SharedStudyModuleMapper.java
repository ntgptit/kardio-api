package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.entity.SharedStudyModule;
import com.kardio.entity.StudyModule;
import com.kardio.entity.User;

/**
 * Mapper for SharedStudyModule entity.
 * Note: This entity doesn't have a direct DTO representation,
 * so this mapper doesn't fully implement the GenericMapper interface.
 */
@Component
public class SharedStudyModuleMapper {

    /**
     * Creates a new SharedStudyModule for a user and study module.
     *
     * @param user        The user
     * @param studyModule The study module
     * @return A new SharedStudyModule entity
     */
    public SharedStudyModule createSharedModule(User user, StudyModule studyModule) {

        if (user == null || studyModule == null) {
            return null;
        }

        return SharedStudyModule.builder().user(user).studyModule(studyModule).build();
    }
}