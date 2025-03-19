package com.kardio.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
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
@Table(name = "session_items", indexes = { @Index(name = "idx_session_items_session_id", columnList = "session_id"),
		@Index(name = "idx_session_items_vocabulary_id", columnList = "vocabulary_id") })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionItem {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private StudySession session;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vocabulary_id", nullable = false)
	private Vocabulary vocabulary;

	@Column(name = "is_correct")
	private Boolean isCorrect;

	@Min(value = 0, message = "Response time cannot be negative")
	@Column(name = "response_time_ms")
	private Integer responseTimeMs;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	/**
	 * Check if the response was fast (under 3 seconds)
	 *
	 * @return true if the response time was under 3 seconds
	 */
	public boolean isFastResponse() {
		return responseTimeMs != null && responseTimeMs < 3000;
	}

	/**
	 * Get the response time in seconds
	 *
	 * @return response time in seconds or null if not available
	 */
	public Double getResponseTimeInSeconds() {
		if (responseTimeMs == null) {
			return null;
		}
		return responseTimeMs / 1000.0;
	}
}