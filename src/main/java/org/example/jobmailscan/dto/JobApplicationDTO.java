package org.example.jobmailscan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.jobmailscan.entity.JobApplication;
import org.example.jobmailscan.entity.OfferStatus;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class JobApplicationDTO {

	private Long id;
	private String gmailMessageId;
	private String gmailThreadId;
	private String companyName;
	private String jobTitle;
	private String platform;
	private LocalDate appliedDate;
	private OfferStatus offerStatus;
	private Integer mark;
	private String jd;
	private String resumeVersion;
	private String emailSubject;
	private String emailFrom;
	private String snippet;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static JobApplicationDTO from(JobApplication e) {
		return JobApplicationDTO.builder()
			.id(e.getId())
			.gmailMessageId(e.getGmailMessageId())
			.gmailThreadId(e.getGmailThreadId())
			.companyName(e.getCompanyName())
			.jobTitle(e.getJobTitle())
			.platform(e.getPlatform())
			.appliedDate(e.getAppliedDate())
			.offerStatus(e.getOfferStatus())
			.mark(e.getMark())
			.jd(e.getJd())
			.resumeVersion(e.getResumeVersion())
			.emailSubject(e.getEmailSubject())
			.emailFrom(e.getEmailFrom())
			.snippet(e.getSnippet())
			.createdAt(e.getCreatedAt())
			.updatedAt(e.getUpdatedAt())
			.build();
	}

	/**
	 * Fields allowed in a PUT request body. Only non-null values are applied.
	 */
	@Data
	@NoArgsConstructor
	public static class UpdateRequest {

		private String companyName;
		private String jobTitle;
		private String platform;
		private LocalDate appliedDate;
		private OfferStatus offerStatus;
		private Integer mark;
		private String jd;
		private String resumeVersion;
		private String emailSubject;
		private String emailFrom;
		private String snippet;
	}
}
