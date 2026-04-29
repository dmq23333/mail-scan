package org.example.jobmailscan.controller;

import java.util.List;

import org.example.jobmailscan.dto.MailSummaryDTO;
import org.example.jobmailscan.service.GmailService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mails")
public class MailController {

	private final GmailService gmailService;

	public MailController(GmailService gmailService) {
		this.gmailService = gmailService;
	}

	@GetMapping("/search")
	public List<MailSummaryDTO> search(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
		@RequestParam(defaultValue = "is:unread") String q) throws Exception {
		return gmailService.getRecentEmails(authorizedClient, q);
	}
}
