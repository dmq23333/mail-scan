package org.example.jobmailscan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailSummaryDTO {

	private String id;
	private String threadId;
	private String from;
	private String subject;
	private String snippet; // mail snippet
	private Long internalDate;
}
