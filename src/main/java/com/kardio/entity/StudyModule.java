package com.kardio.entity;

import java.time.LocalDateTime;

import com.kardio.entity.enums.VisibilityType;

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
@Table(name = "study_modules", indexes = { @Index(name = "idx_study_modules_creator_id", columnList = "creator_id"),
		@Index(name = "idx_study_modules_folder_id", columnList = "folder_id"),
		@Index(name = "idx_study_modules_deleted_at", columnList = "deleted_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyModule extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_id", nullable = false)
	private User creator;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "folder_id")
	private Folder folder;

	@NotBlank(message = "Module name is required")
	@Size(max = 255, message = "Module name cannot exceed 255 characters")
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@NotNull(message = "Visibility is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "visibility", nullable = false, length = 20)
	private VisibilityType visibility;

	@Column(name = "last_studied_at")
	private LocalDateTime lastStudiedAt;

}