package com.kardio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_settings", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "user_id", "setting_key" }) }, indexes = {
				@Index(name = "idx_user_settings_user_id", columnList = "user_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotBlank(message = "Setting key is required")
	@Size(max = 100, message = "Setting key cannot exceed 100 characters")
	@Column(name = "setting_key", nullable = false, length = 100)
	private String settingKey;

	@Column(name = "setting_value", columnDefinition = "TEXT")
	private String settingValue;
}