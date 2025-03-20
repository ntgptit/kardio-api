package com.kardio.constant;

/**
 * Application-wide constants
 */
public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    /**
     * Log message constants
     */
    public static final class LogMessages {
        private LogMessages() {
        }

        public static final String MODULE_NOT_FOUND = "Study module not found with ID: {}";
        public static final String USER_NOT_FOUND = "User not found with ID: {}";
        public static final String VOCABULARY_NOT_FOUND = "Vocabulary not found with ID: {}";
        public static final String FOLDER_NOT_FOUND = "Folder not found with ID: {}";

        public static final String MODULE_DELETED = "Study module deleted successfully: {}";
        public static final String VOCABULARY_DELETED = "Vocabulary deleted successfully: {}";
        public static final String FOLDER_DELETED = "Folder deleted successfully: {}";
    }

    /**
     * Entity name constants for messages
     */
    public static final class EntityNames {
        private EntityNames() {
        }

        public static final String MODULE = "entity.studyModule";
        public static final String USER = "entity.user";
        public static final String VOCABULARY = "entity.vocabulary";
        public static final String FOLDER = "entity.folder";
        public static final String CLASS = "entity.class";
        public static final String PROGRESS = "entity.progress";
    }

    /**
     * Message keys for errors
     */
    public static final class ErrorMessages {
        private ErrorMessages() {
        }

        public static final String RESOURCE_NOT_FOUND = "error.resource.notfound";
        public static final String RESOURCE_ALREADY_EXISTS = "error.resource.alreadyexists";
        public static final String VALIDATION_SEARCH_TERM = "error.validation.searchterm";
        public static final String FORBIDDEN_RESOURCE = "error.forbidden.resource";
        public static final String FORBIDDEN_OWNER = "error.forbidden.owner";
    }

    /**
     * Message keys for success messages
     */
    public static final class SuccessMessages {
        private SuccessMessages() {
        }

        public static final String DELETED = "success.deleted";
        public static final String CREATED = "success.created";
        public static final String UPDATED = "success.updated";
        public static final String SHARED = "success.shared";
    }

    /**
     * Cache names
     */
    public static final class CacheNames {
        private CacheNames() {
        }

        public static final String PUBLIC_MODULES = "publicModules";
        public static final String RECENT_MODULES = "recentModules";
        public static final String MODULE_STATISTICS = "moduleStatistics";
        public static final String FOLDERS = "folders";
        public static final String VOCABULARY_COUNTS = "vocabularyCounts";
    }

    /**
     * JWT related constants
     */
    public static final class JwtConstants {
        private JwtConstants() {
        }

        public static final String TOKEN_TYPE_ACCESS = "access";
        public static final String TOKEN_TYPE_REFRESH = "refresh";
        public static final String AUTHORITIES_KEY = "roles";
        public static final String TOKEN_TYPE_KEY = "type";
        public static final String TOKEN_ID_KEY = "jti";
    }
}