package com.kardio.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "study_sessions", indexes = { @Index(name = "idx_study_sessions_user_id", columnList = "user_id"),
		@Index(name = "idx_study_sessions_module_id", columnList = "module_id") })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySession {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "module_id", nullable = false)
	private StudyModule module;

	@NotNull(message = "Session type is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "session_type", nullable = false, length = 50)
	private SessionType sessionType;

	@Column(name = "start_time", nullable = false)
	@Builder.Default
	private LocalDateTime startTime = LocalDateTime.now();

	@Column(name = "end_time")
	private LocalDateTime endTime;

	@Min(value = 0, message = "Total items cannot be negative")
	@Column(name = "total_items")
	@Builder.Default
	private Integer totalItems = 0;

	@Min(value = 0, message = "Correct items cannot be negative")
	@Column(name = "correct_items")
	@Builder.Default
	private Integer correctItems = 0;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

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

	/**
	 * Calculate the accuracy rate based on correct and total items
	 * 
	 * @return Percentage of correct items (0-100) or 0 if no items
	 */
	public double getAccuracyRate() {
		if (totalItems == 0) {
			return 0;
		}
		return (double) correctItems / totalItems * 100;
	}

	/**
	 * Calculate the duration of the study session
	 * 
	 * @return Duration of the session or null if session hasn't ended
	 */
	public Duration getDuration() {
		if (endTime == null) {
			return null;
		}
		return Duration.between(startTime, endTime);
	}

	/**
	 * End the current study session
	 */
	public void endSession() {
		if (this.endTime == null) {
			this.endTime = LocalDateTime.now();
		}
	}

	/**
	 * Record an item attempt in the session
	 * 
	 * @param isCorrect whether the answer was correct
	 */
	public void recordAttempt(boolean isCorrect) {
		this.totalItems++;
		if (isCorrect) {
			this.correctItems++;
		}
	}
}