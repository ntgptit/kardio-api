package com.kardio.entity.enums;

/**
 * Enum representing the possible roles for a class member
 */
public enum MemberRole {
	TEACHER("teacher"), STUDENT("student"), ADMIN("admin");

	private final String value;

	MemberRole(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
}
