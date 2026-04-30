package org.example.jobmailscan.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.example.jobmailscan.dto.MailSummaryDTO;
import org.example.jobmailscan.service.GmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class MailControllerTest {

	@Mock
	GmailService gmailService;

	@InjectMocks
	MailController controller;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.standaloneSetup(controller)
			.setCustomArgumentResolvers(new MockOAuth2ClientResolver())
			.build();
	}

	// ── GET /api/mails/search ─────────────────────────────────────────────────

	@Test
	void search_defaultQuery_returnsMailList() throws Exception {
		MailSummaryDTO mail = new MailSummaryDTO(
			"msg1", "thread1", "recruiter@acme.com",
			"Your application to Acme", "We received...", 1_700_000_000_000L);
		when(gmailService.getRecentEmails(any(), eq("is:unread"))).thenReturn(List.of(mail));

		mockMvc.perform(get("/api/mails/search"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value("msg1"))
			.andExpect(jsonPath("$[0].from").value("recruiter@acme.com"))
			.andExpect(jsonPath("$[0].subject").value("Your application to Acme"));
	}

	@Test
	void search_customQuery_passesQueryToService() throws Exception {
		when(gmailService.getRecentEmails(any(), eq("subject:interview"))).thenReturn(List.of());

		mockMvc.perform(get("/api/mails/search?q=subject:interview"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void search_multipleResults_returnsAll() throws Exception {
		List<MailSummaryDTO> mails = List.of(
			new MailSummaryDTO("id1", "t1", "a@a.com", "Subject A", "snippet A", 1000L),
			new MailSummaryDTO("id2", "t2", "b@b.com", "Subject B", "snippet B", 2000L),
			new MailSummaryDTO("id3", "t3", "c@c.com", "Subject C", "snippet C", 3000L)
		);
		when(gmailService.getRecentEmails(any(), eq("is:unread"))).thenReturn(mails);

		mockMvc.perform(get("/api/mails/search"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(3))
			.andExpect(jsonPath("$[2].subject").value("Subject C"));
	}

	/**
	 * Injects a Mockito mock for any OAuth2AuthorizedClient parameter.
	 */
	static class MockOAuth2ClientResolver implements HandlerMethodArgumentResolver {

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			return OAuth2AuthorizedClient.class.isAssignableFrom(parameter.getParameterType());
		}

		@Override
		public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
			return mock(OAuth2AuthorizedClient.class);
		}
	}
}
