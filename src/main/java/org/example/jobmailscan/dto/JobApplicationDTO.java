package org.example.jobmailscan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.jobmailscan.entity.JobApplication;
import org.example.jobmailscan.entity.OfferStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
            .emailSubject(e.getEmailSubject())
            .emailFrom(e.getEmailFrom())
            .snippet(e.getSnippet())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }

    /** Fields allowed in a PUT request body. Only non-null values are applied. */
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
    }
}
