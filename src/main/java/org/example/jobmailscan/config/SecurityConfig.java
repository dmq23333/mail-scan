package org.example.jobmailscan.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// 1. Cors support
			.cors(Customizer.withDefaults())

			// 2. CSRF disabled for local dev; re-enable when Angular frontend is ready
			.csrf(csrf -> csrf.disable())

			// 3. Auth control
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/error", "/h2-console/**")
				.permitAll() // allow root url, error, and H2 dev console
				.anyRequest().authenticated() // any other request needs authentication
			)
			// Allow H2 console to render in iframe (uses same-origin frames)
			.headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()))

			// 4. Config OAuth2 login
			.oauth2Login(oauth2 -> oauth2
				.defaultSuccessUrl("http://localhost:4200/") // Redirect to Angular app after successful login
			)

			// 5. Config logout
			.logout(logout -> logout
				.logoutSuccessUrl("http://localhost:4200/")
				.deleteCookies("JSESSIONID")
			);

		return http.build();
	}

	// 2. CORS Config Bean：Allow Angular access from 4200
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:4200")); // 前端地址
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
		configuration.setAllowCredentials(true); // Allow Cookie/Token

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}