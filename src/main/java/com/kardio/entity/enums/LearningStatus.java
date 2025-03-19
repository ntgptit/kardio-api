package com.kardio.entity.enums;

/**
 * Enum representing the possible learning statuses
 */
public enum LearningStatus {
	NOT_STUDIED("not_studied"), LEARNING("learning"), MASTERED("mastered");

	private final String value;

	LearningStatus(String value) {
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