package org.example.jobmailscan.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.example.jobmailscan.dto.JobApplicationDTO;
import org.example.jobmailscan.entity.JobApplication;
import org.example.jobmailscan.entity.OfferStatus;
import org.example.jobmailscan.repository.JobApplicationRepository;
import org.example.jobmailscan.util.JobApplicationClassifier;
import org.example.jobmailscan.util.MailParser;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {

	/**
	 * Gmail search query for job-related emails.
	 * Covers common confirmation, rejection, and interview invitation phrases.
	 */
	private static final String JOB_GMAIL_QUERY =
		"subject:(application OR interview OR offer OR rejection OR \"thank you for applying\" " +
			"OR \"we received your application\" OR \"application received\" OR \"application viewed\" " +
			"OR \"not moving forward\" OR \"next steps\")";

	private static final int PAGE_SIZE = 100;

	private final JobApplicationRepository repository;
	private final NetHttpTransport transport;
	private final JsonFactory jsonFactory;

	public JobApplicationService(JobApplicationRepository repository,
		NetHttpTransport transport,
		JsonFactory jsonFactory) {
		this.repository = repository;
		this.transport = transport;
		this.jsonFactory = jsonFactory;
	}

	// ─── Query ───────────────────────────────────────────────────────────────

	public List<JobApplicationDTO> findAll() {
		return repository.findAllByOrderByAppliedDateDesc()
			.stream().map(JobApplicationDTO::from).toList();
	}

	public List<JobApplicationDTO> findByStatus(OfferStatus status) {
		if (status == null || status == OfferStatus.ALL)
			return findAll();
		return repository.findByOfferStatusOrderByAppliedDateDesc(status)
			.stream().map(JobApplicationDTO::from).toList();
	}

	public JobApplicationDTO findById(Long id) {
		return repository.findById(id)
			.map(JobApplicationDTO::from)
			.orElseThrow(() -> new IllegalArgumentException("Job application not found: " + id));
	}

	public List<JobApplicationDTO> search(String keyword) {
		return repository.findByCompanyNameContainingIgnoreCaseOrJobTitleContainingIgnoreCase(
				keyword, keyword)
			.stream().map(JobApplicationDTO::from).toList();
	}

	public List<JobApplicationDTO> filter(LocalDate appliedAfter, String companyName,
		OfferStatus offerStatus, String resumeVersion, String platform) {

		Specification<JobApplication> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (appliedAfter != null)
				predicates.add(cb.greaterThanOrEqualTo(root.get("appliedDate"), appliedAfter));
			if (companyName != null && !companyName.isBlank())
				predicates.add(cb.like(cb.lower(root.get("companyName")), "%" + companyName.toLowerCase() + "%"));
			if (offerStatus != null && offerStatus != OfferStatus.ALL)
				predicates.add(cb.equal(root.get("offerStatus"), offerStatus));
			if (resumeVersion != null && !resumeVersion.isBlank())
				predicates.add(cb.like(cb.lower(root.get("resumeVersion")), "%" + resumeVersion.toLowerCase() + "%"));
			if (platform != null && !platform.isBlank())
				predicates.add(cb.like(cb.lower(root.get("platform")), "%" + platform.toLowerCase() + "%"));

			return cb.and(predicates.toArray(new Predicate[0]));
		};

		return repository.findAll(spec).stream()
			.sorted((a, b) -> {
				if (a.getAppliedDate() == null) return 1;
				if (b.getAppliedDate() == null) return -1;
				return b.getAppliedDate().compareTo(a.getAppliedDate());
			})
			.map(JobApplicationDTO::from).toList();
	}

	// ─── Sync from Gmail ─────────────────────────────────────────────────────

	/**
	 * Fetches up to MAX_SYNC_RESULTS job-related emails from Gmail,
	 * classifies their status, and upserts them into the database.
	 *
	 * @return list of newly created/updated DTOs
	 */
	@Transactional
	public List<JobApplicationDTO> syncFromGmail(OAuth2AuthorizedClient client, String customQuery)
		throws Exception {

		Gmail gmail = buildGmailService(client);
		String query = (customQuery != null && !customQuery.isBlank()) ? customQuery : JOB_GMAIL_QUERY;

		List<JobApplicationDTO> synced = new ArrayList<>();
		String pageToken = null;

		do {
			ListMessagesResponse listResponse = gmail.users().messages().list("me")
				.setQ(query)
				.setMaxResults((long) PAGE_SIZE)
				.setPageToken(pageToken)
				.execute();

			if (listResponse.getMessages() == null)
				break;

			for (Message stub : listResponse.getMessages()) {
				if (repository.existsByGmailMessageId(stub.getId()))
					continue;

				Message detail = gmail.users().messages().get("me", stub.getId())
					.setFormat("full")
					.execute();

				JobApplication app = buildFromMessage(detail);
				repository.save(app);
				synced.add(JobApplicationDTO.from(app));
			}

			pageToken = listResponse.getNextPageToken();
		} while (pageToken != null);

		return synced;
	}

	// ─── Mutations ───────────────────────────────────────────────────────────

	@Transactional
	public JobApplicationDTO update(Long id, JobApplicationDTO.UpdateRequest req) {
		JobApplication app = repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Job application not found: " + id));

		if (req.getCompanyName() != null)
			app.setCompanyName(req.getCompanyName());
		if (req.getJobTitle() != null)
			app.setJobTitle(req.getJobTitle());
		if (req.getPlatform() != null)
			app.setPlatform(req.getPlatform());
		if (req.getAppliedDate() != null)
			app.setAppliedDate(req.getAppliedDate());
		if (req.getOfferStatus() != null && req.getOfferStatus() != OfferStatus.ALL)
			app.setOfferStatus(req.getOfferStatus());
		if (req.getMark() != null)
			app.setMark(req.getMark());
		if (req.getJd() != null)
			app.setJd(req.getJd());
		if (req.getResumeVersion() != null)
			app.setResumeVersion(req.getResumeVersion());
		if (req.getEmailSubject() != null)
			app.setEmailSubject(req.getEmailSubject());
		if (req.getEmailFrom() != null)
			app.setEmailFrom(req.getEmailFrom());
		if (req.getSnippet() != null)
			app.setSnippet(req.getSnippet());

		return JobApplicationDTO.from(repository.save(app));
	}

	@Transactional
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new IllegalArgumentException("Job application not found: " + id);
		}
		repository.deleteById(id);
	}

	// ─── Helpers ─────────────────────────────────────────────────────────────

	private Gmail buildGmailService(OAuth2AuthorizedClient client) {
		Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
			.setAccessToken(client.getAccessToken().getTokenValue());
		return new Gmail.Builder(transport, jsonFactory, credential)
			.setApplicationName("job-mail-scan")
			.build();
	}

	private JobApplication buildFromMessage(Message message) {
		// Parse headers
		String subject = "";
		String from = "";
		if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
			for (var header : message.getPayload().getHeaders()) {
				if ("Subject".equalsIgnoreCase(header.getName()))
					subject = header.getValue();
				if ("From".equalsIgnoreCase(header.getName()))
					from = header.getValue();
			}
		}

		// Parse body
		String body = MailParser.extractBody(message);

		// Enrich metadata
		String senderName = MailParser.extractSenderName(from);
		String companyName = MailParser.extractCompanyName(subject, senderName);
		String jobTitle = MailParser.extractJobTitle(subject);
		String platform = MailParser.detectPlatform(from);
		OfferStatus status = JobApplicationClassifier.classify(subject, body);

		// Convert epoch-ms → LocalDate
		LocalDate appliedDate = message.getInternalDate() != null
			? Instant.ofEpochMilli(message.getInternalDate())
			  .atZone(ZoneId.systemDefault())
			  .toLocalDate()
			: LocalDate.now();

		JobApplication app = new JobApplication();
		app.setGmailMessageId(message.getId());
		app.setGmailThreadId(message.getThreadId());
		app.setCompanyName(companyName);
		app.setJobTitle(jobTitle);
		app.setPlatform(platform);
		app.setAppliedDate(appliedDate);
		app.setOfferStatus(status);
		app.setEmailSubject(subject);
		app.setEmailFrom(from);
		app.setSnippet(message.getSnippet());
		return app;
	}
}
