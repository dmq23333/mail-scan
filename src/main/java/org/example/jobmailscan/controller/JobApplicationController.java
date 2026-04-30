package org.example.jobmailscan.controller;

import java.util.List;

import org.example.jobmailscan.dto.JobApplicationDTO;
import org.example.jobmailscan.entity.OfferStatus;
import org.example.jobmailscan.service.JobApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobApplicationController {

	private final JobApplicationService service;

	public JobApplicationController(JobApplicationService service) {
		this.service = service;
	}

	/**
	 * GET /api/jobs?status=APPLIED
	 * Returns all job applications, optionally filtered by offer status.
	 * Pass status=ALL (or omit) to get every record.
	 */
	@GetMapping
	public List<JobApplicationDTO> list(
		@RequestParam(required = false, defaultValue = "ALL")
		OfferStatus status
	) {
		return service.findByStatus(status);
	}

	/**
	 * GET /api/jobs/{id}
	 */
	@GetMapping("/{id}")
	public JobApplicationDTO getById(
		@PathVariable
		Long id) {
		return service.findById(id);
	}

	/**
	 * GET /api/jobs/search?q=Google
	 * Full-text keyword search across companyName and jobTitle.
	 */
	@GetMapping("/search")
	public List<JobApplicationDTO> search(
		@RequestParam
		String q) {
		return service.search(q);
	}

	/**
	 * POST /api/jobs/sync?q=<custom gmail query>
	 * Fetches job-related emails from Gmail, classifies them, and persists new records.
	 * Already-synced messages (by gmailMessageId) are skipped.
	 */
	@PostMapping("/sync")
	public List<JobApplicationDTO> sync(
		@RegisteredOAuth2AuthorizedClient("google")
		OAuth2AuthorizedClient client,
		@RequestParam(required = false)
		String q
	) throws Exception {
		return service.syncFromGmail(client, q);
	}

	/**
	 * PUT /api/jobs/{id}
	 * Allows the user to update companyName, jobTitle, platform, appliedDate,
	 * offerStatus, mark, and jd. Only non-null fields are applied.
	 */
	@PutMapping("/{id}")
	public JobApplicationDTO update(
		@PathVariable
		Long id,
		@RequestBody
		JobApplicationDTO.UpdateRequest request
	) {
		return service.update(id, request);
	}

	/**
	 * DELETE /api/jobs/{id}
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
		@PathVariable
		Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
