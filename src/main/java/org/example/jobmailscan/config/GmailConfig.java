package org.example.jobmailscan.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GmailConfig {

	@Bean
	public JsonFactory jsonFactory() {
		return GsonFactory.getDefaultInstance();
	}

	@Bean
	public NetHttpTransport netHttpTransport() {
		return new NetHttpTransport();
	}
}
