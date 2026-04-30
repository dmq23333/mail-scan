package org.example.jobmailscan.service;

import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.example.jobmailscan.dto.MailSummaryDTO;
import org.example.jobmailscan.util.MailParser;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

@Service
public class GmailService {

	private final NetHttpTransport transport;
	private final JsonFactory jsonFactory;

	public GmailService(NetHttpTransport transport, JsonFactory jsonFactory) {
		this.transport = transport;
		this.jsonFactory = jsonFactory;
	}

	public List<MailSummaryDTO> getRecentEmails(OAuth2AuthorizedClient client, String query) throws Exception {
		String tokenValue = client.getAccessToken().getTokenValue();

		// Build a google-api-client Credential using the access token from Spring Security
		Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
			.setAccessToken(tokenValue);

		Gmail service = new Gmail.Builder(transport, jsonFactory, credential)
			.setApplicationName("job-mail-scan")
			.build();

		// 1. 获取消息列表
		ListMessagesResponse listResponse = service.users().messages().list("me")
			.setQ(query)
			.setMaxResults(10L)
			.execute();

		List<MailSummaryDTO> result = new ArrayList<>();
		if (listResponse.getMessages() != null) {
			for (Message msg : listResponse.getMessages()) {
				// 2. get detail info of email and extract header
				Message detail = service.users().messages().get("me", msg.getId()).execute();
				result.add(MailParser.parseToSummary(detail));
			}
		}
		return result;
	}
}