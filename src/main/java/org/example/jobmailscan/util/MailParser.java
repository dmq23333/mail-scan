package org.example.jobmailscan.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.example.jobmailscan.dto.MailSummaryDTO;

public class MailParser {

	// Platform detection by sender domain
	private static final Map<String, String> DOMAIN_TO_PLATFORM = Map.ofEntries(
		Map.entry("linkedin.com", "LinkedIn"),
		Map.entry("indeed.com", "Indeed"),
		Map.entry("greenhouse.io", "Greenhouse"),
		Map.entry("lever.co", "Lever"),
		Map.entry("workday.com", "Workday"),
		Map.entry("myworkdayjobs.com", "Workday"),
		Map.entry("icims.com", "iCIMS"),
		Map.entry("smartrecruiters.com", "SmartRecruiters"),
		Map.entry("jobvite.com", "Jobvite"),
		Map.entry("bamboohr.com", "BambooHR"),
		Map.entry("taleo.net", "Taleo"),
		Map.entry("successfactors.com", "SAP SuccessFactors"),
		Map.entry("workable.com", "Workable"),
		Map.entry("ashbyhq.com", "Ashby"),
		Map.entry("rippling.com", "Rippling"),
		Map.entry("glassdoor.com", "Glassdoor"),
		Map.entry("ziprecruiter.com", "ZipRecruiter"),
		Map.entry("monster.com", "Monster")
	);

	// Patterns to extract job title from subject
	private static final List<Pattern> JOB_TITLE_PATTERNS = List.of(
		Pattern.compile("(?i)application\\s+for\\s+(.+?)\\s+at\\s+", Pattern.CASE_INSENSITIVE),
		Pattern.compile("(?i)your\\s+application\\s+for\\s+(.+?)\\s+(?:at|to|has|is|–|-)", Pattern.CASE_INSENSITIVE),
		Pattern.compile("(?i)interview\\s+(?:invitation|invite)\\s*[:\\-]\\s*(.+?)\\s+(?:at|to|with|–|-)",
			Pattern.CASE_INSENSITIVE),
		Pattern.compile("(?i)(?:re|regarding):\\s*(.+?)\\s+(?:at|to|–|-|\\[)", Pattern.CASE_INSENSITIVE),
		Pattern.compile("(?i)(.+?)\\s*[-–]\\s*application\\s+(?:received|confirmed|submitted)",
			Pattern.CASE_INSENSITIVE),
		Pattern.compile("(?i)(.+?)\\s+position", Pattern.CASE_INSENSITIVE)
	);

	// Patterns to extract company name from subject
	private static final List<Pattern> COMPANY_FROM_SUBJECT_PATTERNS = List.of(
		Pattern.compile(
			"(?i)(?:at|to|from|with)\\s+([A-Z][\\w\\s&,.']+?)(?:\\s*[,!\\?]|\\s*$|\\s+(?:has|is|for|have|about|team|\\())",
			Pattern.CASE_INSENSITIVE),
		Pattern.compile("(?i)application\\s+to\\s+([A-Z][\\w\\s&,.']+?)(?:\\s*[:\\-,!]|\\s*$)",
			Pattern.CASE_INSENSITIVE),
		Pattern.compile("(?i)([A-Z][\\w\\s&,.']+?)\\s+(?:viewed|reviewed|has|team|is)", Pattern.CASE_INSENSITIVE)
	);

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
		for (Map.Entry<String, String> entry : DOMAIN_TO_PLATFORM.entrySet()) {
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
			for (Pattern p : COMPANY_FROM_SUBJECT_PATTERNS) {
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
		for (Pattern p : JOB_TITLE_PATTERNS) {
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
