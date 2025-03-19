package com.kardio.entity.enums;

/**
 * Enum representing the possible difficulty levels for a vocabulary
 */
public enum DifficultyLevel {
	EASY("easy"), MEDIUM("medium"), HARD("hard");

	private final String value;

	DifficultyLevel(String value) {
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
