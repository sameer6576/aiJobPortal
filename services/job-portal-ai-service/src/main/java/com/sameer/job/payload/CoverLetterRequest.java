package com.sameer.job.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetterRequest {
    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String jobDescription;

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    private String candidateSummary;
    private List<String> candidateSkills;
    private List<String> candidateExperience;
    private String targetCompanyName;
}
