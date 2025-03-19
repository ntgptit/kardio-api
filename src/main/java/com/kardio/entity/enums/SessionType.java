package com.kardio.entity.enums;

/**
 * Enum representing the possible study session types
 */
public enum SessionType {
	FLASHCARD("flashcard"), LEARN("learn"), TEST("test"), MATCH("match"), BLAST("blast");

	private final String value;

	SessionType(String value) {
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