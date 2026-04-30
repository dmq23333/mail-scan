package org.example.jobmailscan.controller;

import java.time.LocalDate;
import java.util.List;

import org.example.jobmailscan.dto.JobApplicationDTO;
import org.example.jobmailscan.entity.OfferStatus;
import org.example.jobmailscan.service.JobApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

	private final JobApplicationService service;

	public ApplicationController(JobApplicationService service) {
		this.service = service;
	}

	/**
	 * GET /api/applications
	 * All params are optional; omitting a param means no filter on that field.
	 *
	 * @param appliedAfter  include records with appliedDate >= this date (yyyy-MM-dd)
	 * @param companyName   partial match, case-insensitive
	 * @param offerStatus   exact match (APPLIED / REJECTED / etc.), ALL = no filter
	 * @param resumeVersion partial match, case-insensitive
	 * @param platform      partial match, case-insensitive
	 */
	@GetMapping
	public ResponseEntity<List<JobApplicationDTO>> search(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate appliedAfter,
		@RequestParam(required = false)
		String companyName,
		@RequestParam(required = false)
		OfferStatus offerStatus,
		@RequestParam(required = false)
		String resumeVersion,
		@RequestParam(required = false)
		String platform) {

		return ResponseEntity.ok(service.filter(appliedAfter, companyName, offerStatus, resumeVersion, platform));
	}

	/**
	 * PUT /api/applications/{id}
	 * Updates all editable fields (non-null values only). id is read-only.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<JobApplicationDTO> update(
		@PathVariable
		Long id,
		@RequestBody
		JobApplicationDTO.UpdateRequest req) {

		return ResponseEntity.ok(service.update(id, req));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
		@PathVariable
		Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
