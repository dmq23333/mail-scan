package org.example.jobmailscan.util;

import org.example.jobmailscan.entity.OfferStatus;

import java.util.List;

/**
 * Keyword-based classifier that infers an OfferStatus from email subject + body.
 * Priority order (highest to lowest):
 * INTERVIEW_INVITATION → REJECTED → APPLICATION_VIEWED → APPLIED → WAITING_RESPONSE
 */
public class JobApplicationClassifier {

	private static final List<String> INTERVIEW_KEYWORDS = List.of(
		"interview",
		"phone screen",
		"video call",
		"hiring manager",
		"schedule a call",
		"schedule a meeting",
		"invite you to",
		"would like to speak",
		"would like to chat",
		"next step",
		"next steps",
		"technical assessment",
		"coding challenge",
		"take-home",
		"on-site",
		"onsite"
	);

	private static final List<String> REJECTED_KEYWORDS = List.of(
		"unfortunately",
		"regret to inform",
		"not moving forward",
		"not be moving forward",
		"won't be moving forward",
		"decided to move forward with other",
		"other candidates",
		"not selected",
		"position has been filled",
		"not be proceeding",
		"not a match",
		"we will not",
		"we won't",
		"no longer considering",
		"have decided not",
		"decided not to move",
		"moved forward with another",
		"filled the position",
		"not the right fit",
		"not be the right fit"
	);

	private static final List<String> VIEWED_KEYWORDS = List.of(
		"viewed your application",
		"reviewed your application",
		"your application has been viewed",
		"your application has been reviewed",
		"your profile has been viewed",
		"shortlisted",
		"under review",
		"being reviewed",
		"application is under review"
	);

	private static final List<String> APPLIED_KEYWORDS = List.of(
		"received your application",
		"thank you for applying",
		"application received",
		"successfully applied",
		"we've received",
		"we have received",
		"application has been submitted",
		"application has been received",
		"your application to",
		"application confirmation",
		"applied successfully"
	);

	/**
	 * Classifies status from subject + body text.
	 * Both are lower-cased internally — callers need not pre-process.
	 */
	public static OfferStatus classify(String subject, String body) {
		String combined = ((subject == null ? "" : subject) + " " + (body == null ? "" : body))
			.toLowerCase();

		if (containsAny(combined, INTERVIEW_KEYWORDS))
			return OfferStatus.INTERVIEW_INVITATION;
		if (containsAny(combined, REJECTED_KEYWORDS))
			return OfferStatus.REJECTED;
		if (containsAny(combined, VIEWED_KEYWORDS))
			return OfferStatus.APPLICATION_VIEWED;
		if (containsAny(combined, APPLIED_KEYWORDS))
			return OfferStatus.APPLIED;

		return OfferStatus.WAITING_RESPONSE;
	}

	private static boolean containsAny(String text, List<String> keywords) {
		for (String kw : keywords) {
			if (text.contains(kw))
				return true;
		}
		return false;
	}
}
