package com.sameer.job.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCertificationRequest {
    @NotBlank(message = "Certification name is required")
    @Size(max = 200, message = "Certification name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Issuing organization is required")
    @Size(max = 200, message = "Issuing organization must not exceed 200 characters")
    private String issuingOrganization;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Size(max = 100, message = "Credential ID must not exceed 100 characters")
    private String credentialId;

    @Pattern(regexp = "^$|^(https?://).*", message = "Credential URL must be valid")
    private String credentialUrl;

    private Integer displayOrder;
}
