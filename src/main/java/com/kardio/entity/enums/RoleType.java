package com.kardio.entity.enums;

/**
 * Enum for standard role types in the system.
 */
public enum RoleType {
    ADMIN("ADMIN"), USER("USER");

    private final String value;

    RoleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Find a RoleType by string value
     *
     * @param value The string value of the role
     * @return The matching RoleType, or null if not found
     */
    public static RoleType fromValue(String value) {
        for (RoleType roleType : RoleType.values()) {
            if (roleType.value.equals(value)) {
                return roleType;
            }
        }
        return null;
    }
}