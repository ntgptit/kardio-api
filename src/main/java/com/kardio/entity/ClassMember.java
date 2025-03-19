package com.kardio.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kardio.entity.enums.MemberRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "class_members", indexes = { @Index(name = "idx_class_members_class_id", columnList = "class_id"),
		@Index(name = "idx_class_members_user_id", columnList = "user_id") })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassMember {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_id", nullable = false)
	private Class classEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotNull(message = "Member role is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	@Builder.Default
	private MemberRole role = MemberRole.STUDENT;

	@CreatedDate
	@Column(name = "joined_at", updatable = false)
	private LocalDateTime joinedAt;

	/**
	 * Check if the member has teacher or admin privileges
	 *
	 * @return true if the member is a teacher or admin
	 */
	public boolean hasTeachingPrivileges() {
		return role == MemberRole.TEACHER || role == MemberRole.ADMIN;
	}

	/**
	 * Check if the member is an administrator
	 *
	 * @return true if the member is an admin
	 */
	public boolean isAdmin() {
		return role == MemberRole.ADMIN;
	}
}
