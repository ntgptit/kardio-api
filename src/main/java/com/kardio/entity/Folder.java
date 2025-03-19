package com.kardio.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "folders", indexes = { @Index(name = "idx_folders_user_id", columnList = "user_id"),
		@Index(name = "idx_folders_deleted_at", columnList = "deleted_at"),
		@Index(name = "idx_folders_parent_id", columnList = "parent_folder_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotBlank(message = "Folder name is required")
	@Size(max = 255, message = "Folder name cannot exceed 255 characters")
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_folder_id")
	private Folder parentFolder;

	@OneToMany(mappedBy = "parentFolder", fetch = FetchType.LAZY)
	@Builder.Default
	private List<Folder> subfolders = new ArrayList<>();
}