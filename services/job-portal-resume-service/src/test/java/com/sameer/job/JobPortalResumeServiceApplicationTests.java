package com.sameer.job;

import com.sameer.job.dto.PersonalInfoResponse;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JobPortalResumeServiceApplicationTests {

	@Autowired
	private Validator validator;

	@Test
	void contextLoads() {
	}

	@Test
	void acceptsValidPersonalInfoUrls() {
		PersonalInfoResponse request = PersonalInfoResponse.builder()
				.linkedinUrl("https://www.linkedin.com/in/sameer")
				.githubUrl("https://github.com/sameer")
				.portfolioUrl("https://portfolio.example.com")
				.websiteUrl("")
				.build();

		assertTrue(validator.validate(request).isEmpty());
	}

	@Test
	void rejectsInvalidOrWrongDomainPersonalInfoUrls() {
		PersonalInfoResponse request = PersonalInfoResponse.builder()
				.linkedinUrl("https://example.com/not-linkedin")
				.githubUrl("not-a-url")
				.portfolioUrl("ftp://example.com")
				.build();

		assertFalse(validator.validate(request).isEmpty());
	}

}
