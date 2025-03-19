package com.kardio.entity;

import com.kardio.entity.enums.DifficultyLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vocabularies", indexes = { @Index(name = "idx_vocabularies_module_id", columnList = "module_id"),
		@Index(name = "idx_vocabularies_term", columnList = "term"),
		@Index(name = "idx_vocabularies_deleted_at", columnList = "deleted_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vocabulary extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "module_id", nullable = false)
	private StudyModule module;

	@NotBlank(message = "Term is required")
	@Size(max = 255, message = "Term cannot exceed 255 characters")
	@Column(name = "term", nullable = false)
	private String term;

	@NotBlank(message = "Definition is required")
	@Column(name = "definition", nullable = false, columnDefinition = "TEXT")
	private String definition;

	@Column(name = "example", columnDefinition = "TEXT")
	private String example;

	@Column(name = "pronunciation")
	@Size(max = 255, message = "Pronunciation cannot exceed 255 characters")
	private String pronunciation;

	@Column(name = "part_of_speech", length = 50)
	@Size(max = 50, message = "Part of speech cannot exceed 50 characters")
	private String partOfSpeech;

	@NotNull(message = "Difficulty level is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "difficulty_level", length = 10)
	@Builder.Default
	private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;
}