package org.example.jobmailscan.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ScanConstants {

	private ScanConstants() {
	}

	// ─── Gmail Sync ───────────────────────────────────────────────────────────

	public static final String JOB_GMAIL_QUERY =
		"subject:(application OR interview OR offer OR rejection OR \"thank you for applying\" " +
			"OR \"we received your application\" OR \"application received\" OR \"application viewed\" " +
			"OR \"not moving forward\" OR \"next steps\")";

	/**
	 * Emails from these senders are skipped during sync.
	 */
	public static final List<String> SKIPPED_SENDERS = List.of(
		"donotreply@jora.com"
	);

	// ─── Platform Detection ───────────────────────────────────────────────────

	public static final Map<String, String> DOMAIN_TO_PLATFORM = Map.ofEntries(
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

	// ─── Mail Parsing Patterns ────────────────────────────────────────────────

	public static final List<Pattern> JOB_TITLE_PATTERNS = List.of(
		Pattern.compile("(?i)application\\s+for\\s+(.+?)\\s+at\\s+"),
		Pattern.compile("(?i)your\\s+application\\s+for\\s+(.+?)\\s+(?:at|to|has|is|–|-)"),
		Pattern.compile("(?i)interview\\s+(?:invitation|invite)\\s*[:\\-]\\s*(.+?)\\s+(?:at|to|with|–|-)"),
		Pattern.compile("(?i)(?:re|regarding):\\s*(.+?)\\s+(?:at|to|–|-|\\[)"),
		Pattern.compile("(?i)(.+?)\\s*[-–]\\s*application\\s+(?:received|confirmed|submitted)"),
		Pattern.compile("(?i)(.+?)\\s+position")
	);

	public static final List<Pattern> COMPANY_FROM_SUBJECT_PATTERNS = List.of(
		Pattern.compile(
			"(?i)(?:at|to|from|with)\\s+([A-Z][\\w\\s&,.']+?)(?:\\s*[,!\\?]|\\s*$|\\s+(?:has|is|for|have|about|team|\\())"),
		Pattern.compile("(?i)application\\s+to\\s+([A-Z][\\w\\s&,.']+?)(?:\\s*[:\\-,!]|\\s*$)"),
		Pattern.compile("(?i)([A-Z][\\w\\s&,.']+?)\\s+(?:viewed|reviewed|has|team|is)")
	);
}
