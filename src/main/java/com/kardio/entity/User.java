package com.kardio.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a system user
 */
@Entity
@Table(name = "users", indexes = { @Index(name = "idx_users_email", columnList = "email"),
		@Index(name = "idx_users_deleted_at", columnList = "deleted_at") })
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters long")
	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "active", nullable = false)
	@Builder.Default
	private boolean active = true;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	@Builder.Default
	private Set<Role> roles = new HashSet<>();

	/**
	 * Add a role to this user
	 *
	 * @param role The role to add
	 */
	public void addRole(Role role) {
		if (this.roles == null) {
			this.roles = new HashSet<>();
		}
		this.roles.add(role);
	}

	/**
	 * Remove a role from this user
	 *
	 * @param role The role to remove
	 * @return true if the role was removed, false if not found
	 */
	public boolean removeRole(Role role) {
		if (this.roles == null) {
			return false;
		}
		return this.roles.remove(role);
	}

	/**
	 * Check if the user has a specific role
	 *
	 * @param roleName The role name to check
	 * @return true if the user has the role, false otherwise
	 */
	public boolean hasRole(String roleName) {
		if (this.roles == null) {
			return false;
		}
		return this.roles.stream().anyMatch(role -> role.getName().equals(roleName));
	}
}