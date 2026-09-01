package com.sameer.job.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalInfoResponse {

    private String firstName;

    private String lastName;

    private String headline;

    private String email;
    private String phone;
    private String city;
    private String country;

    @URL(message = "LinkedIn URL must be a valid URL")
    @Pattern(
            regexp = "^$|https?://(?:[A-Za-z0-9-]+\\.)*linkedin\\.com(?:/.*)?$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "LinkedIn URL must use linkedin.com and start with http:// or https://"
    )
    @Size(max = 2048, message = "LinkedIn URL must not exceed 2048 characters")
    private String linkedinUrl;

    @URL(message = "GitHub URL must be a valid URL")
    @Pattern(
            regexp = "^$|https?://(?:[A-Za-z0-9-]+\\.)*github\\.com(?:/.*)?$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "GitHub URL must use github.com and start with http:// or https://"
    )
    @Size(max = 2048, message = "GitHub URL must not exceed 2048 characters")
    private String githubUrl;

    @URL(message = "Portfolio URL must be a valid URL")
    @Pattern(regexp = "^$|https?://.*$", message = "Portfolio URL must start with http:// or https://")
    @Size(max = 2048, message = "Portfolio URL must not exceed 2048 characters")
    private String portfolioUrl;

    @URL(message = "Website URL must be a valid URL")
    @Pattern(regexp = "^$|https?://.*$", message = "Website URL must start with http:// or https://")
    @Size(max = 2048, message = "Website URL must not exceed 2048 characters")
    private String websiteUrl;

}
