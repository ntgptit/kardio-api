package com.kardio.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "streaks", indexes = { @Index(name = "idx_streaks_user_id", columnList = "user_id") })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Streak {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Min(value = 0, message = "Current streak cannot be negative")
	@Column(name = "current_streak")
	@Builder.Default
	private Integer currentStreak = 0;

	@Min(value = 0, message = "Longest streak cannot be negative")
	@Column(name = "longest_streak")
	@Builder.Default
	private Integer longestStreak = 0;

	@Column(name = "last_activity_date")
	private LocalDate lastActivityDate;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	/**
	 * Record user activity for today Updates the streak count based on the days
	 * since last activity
	 */
	public void recordActivity() {
		LocalDate today = LocalDate.now();

		if (lastActivityDate == null) {
			// First activity ever
			currentStreak = 1;
		} else {
			long daysSinceLastActivity = ChronoUnit.DAYS.between(lastActivityDate, today);

			if (daysSinceLastActivity == 0) {
				// Already recorded activity today
				return;
			} else if (daysSinceLastActivity == 1) {
				// Consecutive day - increase streak
				currentStreak++;
			} else {
				// Streak broken - reset to 1
				currentStreak = 1;
			}
		}

		// Update longest streak if needed
		if (currentStreak > longestStreak) {
			longestStreak = currentStreak;
		}

		lastActivityDate = today;
	}

	/**
	 * Check if user is active today
	 *
	 * @return true if user has already recorded activity today
	 */
	public boolean isActiveToday() {
		return lastActivityDate != null && lastActivityDate.equals(LocalDate.now());
	}
}