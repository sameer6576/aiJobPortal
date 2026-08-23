package com.sameer.job.payload;

import lombok.Data;

import java.util.List;

@Data
public class ScreeningScoreRequest {
    private String jobTitle;
    private String experienceLevel;
    private List<String> requiredSkills;
    private String responsibilities;
    private String candidateSummary;
    private List<String> candidateSkills;
    private List<String> candidateExperience;
}
