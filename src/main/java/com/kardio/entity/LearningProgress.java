package com.kardio.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kardio.entity.enums.LearningStatus;

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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "learning_progress", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "user_id", "vocabulary_id" }) }, indexes = {
				@Index(name = "idx_learning_progress_user_id", columnList = "user_id"),
				@Index(name = "idx_learning_progress_vocabulary_id", columnList = "vocabulary_id"),
				@Index(name = "idx_learning_progress_status", columnList = "status"),
				@Index(name = "idx_learning_progress_next_review", columnList = "next_review_at") })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgress {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vocabulary_id", nullable = false)
	private Vocabulary vocabulary;

	@NotNull(message = "Learning status is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private LearningStatus status = LearningStatus.NOT_STUDIED;

	@Min(value = 0, message = "Correct count cannot be negative")
	@Column(name = "correct_count")
	@Builder.Default
	private Integer correctCount = 0;

	@Min(value = 0, message = "Incorrect count cannot be negative")
	@Column(name = "incorrect_count")
	@Builder.Default
	private Integer incorrectCount = 0;

	@Column(name = "last_studied_at")
	private LocalDateTime lastStudiedAt;

	@Column(name = "next_review_at")
	private LocalDateTime nextReviewAt;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	/**
	 * Calculate the accuracy rate based on correct and incorrect counts
	 * 
	 * @return Percentage of correct answers (0-100) or 0 if no answers yet
	 */
	public double getAccuracyRate() {
		int total = correctCount + incorrectCount;
		if (total == 0) {
			return 0;
		}
		return (double) correctCount / total * 100;
	}

	/**
	 * Record a study attempt
	 * 
	 * @param isCorrect      whether the answer was correct
	 * @param nextReviewTime when to schedule the next review
	 */
	public void recordAttempt(boolean isCorrect, LocalDateTime nextReviewTime) {
		if (isCorrect) {
			this.correctCount++;
		} else {
			this.incorrectCount++;
		}

		this.lastStudiedAt = LocalDateTime.now();
		this.nextReviewAt = nextReviewTime;

		// Update the learning status based on performance
		updateStatus();
	}

	/**
	 * Update the learning status based on correct and incorrect counts
	 */
	private void updateStatus() {
		int total = correctCount + incorrectCount;

		if (total == 0) {
			this.status = LearningStatus.NOT_STUDIED;
		} else if (correctCount >= 5 && getAccuracyRate() >= 90) {
			this.status = LearningStatus.MASTERED;
		} else {
			this.status = LearningStatus.LEARNING;
		}
	}

}