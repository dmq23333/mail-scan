package org.example.jobmailscan.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.example.jobmailscan.dto.MailSummaryDTO;

public class MailParser {

	public static MailSummaryDTO parseToSummary(Message message) {
		String subject = "";
		String from = "";

		if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
			for (MessagePartHeader header : message.getPayload().getHeaders()) {
				if ("Subject".equalsIgnoreCase(header.getName()))
					subject = header.getValue();
				if ("From".equalsIgnoreCase(header.getName()))
					from = header.getValue();
			}
		}

		return new MailSummaryDTO(
			message.getId(),
			message.getThreadId(),
			from,
			subject,
			message.getSnippet(),
			message.getInternalDate()
		);
	}

	/**
	 * Extracts plain-text body from a Gmail Message (handles simple + multipart).
	 */
	public static String extractBody(Message message) {
		if (message.getPayload() == null)
			return "";
		return extractBodyFromPart(message.getPayload());
	}

	private static String extractBodyFromPart(MessagePart part) {
		if (part == null)
			return "";

		// Try text/plain first for clean keyword matching
		if ("text/plain".equalsIgnoreCase(part.getMimeType())) {
			return decodeData(part.getBody() != null ? part.getBody().getData() : null);
		}

		// Recurse into multipart
		if (part.getParts() != null) {
			for (MessagePart child : part.getParts()) {
				if ("text/plain".equalsIgnoreCase(child.getMimeType())) {
					String text = decodeData(child.getBody() != null ? child.getBody().getData() : null);
					if (!text.isBlank())
						return text;
				}
			}
			// Fallback: any non-null part
			for (MessagePart child : part.getParts()) {
				String text = extractBodyFromPart(child);
				if (!text.isBlank())
					return text;
			}
		}

		// Fallback to whatever data is in this part
		if (part.getBody() != null) {
			return decodeData(part.getBody().getData());
		}
		return "";
	}

	private static String decodeData(String data) {
		if (data == null || data.isBlank())
			return "";
		try {
			byte[] decoded = Base64.getUrlDecoder().decode(data);
			return new String(decoded, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			return "";
		}
	}

	/**
	 * Detects the job platform from the sender's email domain.
	 */
	public static String detectPlatform(String from) {
		String domain = extractEmailDomain(from);
		if (domain == null)
			return "Unknown";
		for (Map.Entry<String, String> entry : ScanConstants.DOMAIN_TO_PLATFORM.entrySet()) {
			if (domain.endsWith(entry.getKey()))
				return entry.getValue();
		}
		return "Direct";
	}

	/**
	 * Extracts a human-readable sender name from the From header. Falls back to domain.
	 */
	public static String extractSenderName(String from) {
		if (from == null || from.isBlank())
			return "";
		// "Company Name" <email@company.com>  or  Company Name <email@company.com>
		Pattern nameAngle = Pattern.compile("^\"?([^\"<]+?)\"?\\s*<");
		Matcher m = nameAngle.matcher(from.trim());
		if (m.find()) {
			return m.group(1).trim();
		}
		// bare email address — use domain
		String domain = extractEmailDomain(from);
		return domain != null ? domain : from;
	}

	/**
	 * Attempts to extract company name from subject, with senderName as fallback.
	 */
	public static String extractCompanyName(String subject, String senderName) {
		if (subject != null && !subject.isBlank()) {
			for (Pattern p : ScanConstants.COMPANY_FROM_SUBJECT_PATTERNS) {
				Matcher m = p.matcher(subject);
				if (m.find()) {
					String candidate = m.group(1).trim();
					// Reject single-word generics
					if (candidate.length() > 2 && !isGenericWord(candidate)) {
						return candidate;
					}
				}
			}
		}
		return senderName != null ? senderName : "";
	}

	/**
	 * Attempts to extract job title from subject line.
	 */
	public static String extractJobTitle(String subject) {
		if (subject == null || subject.isBlank())
			return "";
		for (Pattern p : ScanConstants.JOB_TITLE_PATTERNS) {
			Matcher m = p.matcher(subject);
			if (m.find()) {
				String candidate = m.group(1).trim();
				if (candidate.length() > 2 && candidate.length() < 100 && !isGenericWord(candidate)) {
					return candidate;
				}
			}
		}
		return subject.length() <= 80 ? subject : subject.substring(0, 80);
	}

	private static String extractEmailDomain(String from) {
		if (from == null)
			return null;
		Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+\\-]+@([a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,})");
		Matcher m = emailPattern.matcher(from);
		return m.find() ? m.group(1).toLowerCase() : null;
	}

	private static boolean isGenericWord(String word) {
		String lower = word.toLowerCase().trim();
		return lower.equals("your") || lower.equals("the") || lower.equals("our")
			|| lower.equals("a") || lower.equals("an") || lower.equals("this");
	}
}
