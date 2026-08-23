package com.sameer.job.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningScoreRequest {
    private String jobTitle;
    private String experienceLevel;
    private List<String> requiredSkills;
    private String responsibilities;
    private String candidateSummary;
    private List<String> candidateSkills;
    private List<String> candidateExperience;
}
