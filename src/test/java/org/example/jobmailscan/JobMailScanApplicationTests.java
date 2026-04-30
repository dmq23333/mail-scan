package org.example.jobmailscan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
	"spring.security.oauth2.client.registration.google.client-id=test-client-id",
	"spring.security.oauth2.client.registration.google.client-secret=test-client-secret"
})
@ActiveProfiles("test")
class JobMailScanApplicationTests {

	@Test
	void contextLoads() {
	}

}
