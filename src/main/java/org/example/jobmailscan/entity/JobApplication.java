package org.example.jobmailscan.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
public class JobApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Gmail message ID
	 */
	@Column(name = "gmail_message_id", unique = true, nullable = false)
	private String gmailMessageId;

	@Column(name = "gmail_thread_id")
	private String gmailThreadId;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "job_title")
	private String jobTitle;

	/**
	 * Sourcing platform: LinkedIn, Indeed, Greenhouse, etc.
	 */
	@Column(name = "platform")
	private String platform;

	/**
	 * Date the email was received / application was sent.
	 */
	@Column(name = "applied_date")
	private LocalDate appliedDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "offer_status", nullable = false)
	private OfferStatus offerStatus;

	/**
	 * User-defined rating (1–5). Null until the user sets it.
	 */
	@Column(name = "mark")
	private Integer mark;

	/**
	 * Full job description text (populated manually or by user).
	 */
	@Column(name = "jd", columnDefinition = "TEXT")
	private String jd;

	@Column(name = "resume_version", length = 250)
	private String resumeVersion;

	/**
	 * Raw email subject, for reference.
	 */
	@Column(name = "email_subject")
	private String emailSubject;

	/**
	 * Raw email sender, for reference.
	 */
	@Column(name = "email_from")
	private String emailFrom;

	/**
	 * Snippet from the Gmail API.
	 */
	@Column(name = "snippet", columnDefinition = "TEXT")
	private String snippet;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
