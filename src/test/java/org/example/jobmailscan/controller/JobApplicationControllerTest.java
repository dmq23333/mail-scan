package org.example.jobmailscan.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.example.jobmailscan.dto.JobApplicationDTO;
import org.example.jobmailscan.entity.OfferStatus;
import org.example.jobmailscan.service.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class JobApplicationControllerTest {

	@Mock
	JobApplicationService service;

	@InjectMocks
	JobApplicationController controller;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.standaloneSetup(controller)
			.setCustomArgumentResolvers(new MockOAuth2ClientResolver())
			.build();
	}

	private JobApplicationDTO sampleDto(Long id) {
		JobApplicationDTO dto = new JobApplicationDTO();
		dto.setId(id);
		dto.setGmailMessageId("msg-" + id);
		dto.setCompanyName("Acme");
		dto.setJobTitle("Software Engineer");
		dto.setOfferStatus(OfferStatus.APPLIED);
		dto.setAppliedDate(LocalDate.of(2026, 1, 15));
		return dto;
	}

	// ── GET /api/jobs ─────────────────────────────────────────────────────────

	@Test
	void list_noFilter_returnsAllRecords() throws Exception {
		when(service.findByStatus(OfferStatus.ALL)).thenReturn(List.of(sampleDto(1L), sampleDto(2L)));

		mockMvc.perform(get("/api/jobs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].companyName").value("Acme"))
			.andExpect(jsonPath("$[0].offerStatus").value("APPLIED"));
	}

	@Test
	void list_withStatusFilter_returnsFilteredRecords() throws Exception {
		when(service.findByStatus(OfferStatus.REJECTED)).thenReturn(List.of());

		mockMvc.perform(get("/api/jobs?status=REJECTED"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	// ── GET /api/jobs/{id} ────────────────────────────────────────────────────

	@Test
	void getById_existingId_returnsDto() throws Exception {
		when(service.findById(1L)).thenReturn(sampleDto(1L));

		mockMvc.perform(get("/api/jobs/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.jobTitle").value("Software Engineer"));
	}

	@Test
	void getById_notFound_throwsException() {
		when(service.findById(99L)).thenThrow(new IllegalArgumentException("Job application not found: 99"));

		assertThrows(Exception.class, () -> mockMvc.perform(get("/api/jobs/99")));
	}

	// ── GET /api/jobs/search ──────────────────────────────────────────────────

	@Test
	void search_matchingKeyword_returnsResults() throws Exception {
		when(service.search("Acme")).thenReturn(List.of(sampleDto(1L)));

		mockMvc.perform(get("/api/jobs/search?q=Acme"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].companyName").value("Acme"));
	}

	@Test
	void search_noMatch_returnsEmptyList() throws Exception {
		when(service.search("Unknown")).thenReturn(List.of());

		mockMvc.perform(get("/api/jobs/search?q=Unknown"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	// ── POST /api/jobs/sync ───────────────────────────────────────────────────

	@Test
	void sync_noQuery_syncsAndReturnsNewRecords() throws Exception {
		when(service.syncFromGmail(any(), isNull())).thenReturn(List.of(sampleDto(1L), sampleDto(2L)));

		mockMvc.perform(post("/api/jobs/sync"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void sync_withCustomQuery_passesQueryToService() throws Exception {
		when(service.syncFromGmail(any(), eq("subject:interview"))).thenReturn(List.of(sampleDto(1L)));

		mockMvc.perform(post("/api/jobs/sync?q=subject:interview"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));
	}

	// ── PUT /api/jobs/{id} ────────────────────────────────────────────────────

	@Test
	void update_validRequest_returnsUpdatedDto() throws Exception {
		JobApplicationDTO updated = sampleDto(1L);
		updated.setOfferStatus(OfferStatus.INTERVIEW_INVITATION);
		updated.setMark(5);
		when(service.update(eq(1L), any())).thenReturn(updated);

		mockMvc.perform(put("/api/jobs/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"offerStatus\":\"INTERVIEW_INVITATION\",\"mark\":5}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.offerStatus").value("INTERVIEW_INVITATION"))
			.andExpect(jsonPath("$.mark").value(5));
	}

	@Test
	void update_partialFields_onlyUpdatesProvidedFields() throws Exception {
		JobApplicationDTO updated = sampleDto(1L);
		updated.setCompanyName("NewCorp");
		when(service.update(eq(1L), any())).thenReturn(updated);

		mockMvc.perform(put("/api/jobs/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"companyName\":\"NewCorp\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.companyName").value("NewCorp"));
	}

	// ── DELETE /api/jobs/{id} ─────────────────────────────────────────────────

	@Test
	void delete_existingRecord_returns204() throws Exception {
		doNothing().when(service).delete(1L);

		mockMvc.perform(delete("/api/jobs/1"))
			.andExpect(status().isNoContent());

		verify(service).delete(1L);
	}

	@Test
	void delete_notFound_throwsException() {
		doThrow(new IllegalArgumentException("Job application not found: 99"))
			.when(service).delete(99L);

		assertThrows(Exception.class, () -> mockMvc.perform(delete("/api/jobs/99")));
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
