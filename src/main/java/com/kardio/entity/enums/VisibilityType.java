package com.kardio.entity.enums;

/**
 * Enum representing the possible visibility types for a study module
 */
public enum VisibilityType {
	PRIVATE("private"), PUBLIC("public"), SHARED("shared");

	private final String value;

	VisibilityType(String value) {
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