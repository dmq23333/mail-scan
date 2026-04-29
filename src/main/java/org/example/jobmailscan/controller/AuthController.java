package org.example.jobmailscan.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

	@GetMapping("/api/me")
	public Map<String, Object> user(@AuthenticationPrincipal
	OAuth2User principal) {
		// Return User's basic google information to frontend
		return principal.getAttributes();
	}
}
