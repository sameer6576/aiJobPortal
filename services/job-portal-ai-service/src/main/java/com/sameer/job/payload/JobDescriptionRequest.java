package com.sameer.job.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class JobDescriptionRequest {
    @NotBlank(message = "Job title is required")
    private String jobTitle;
    private List<String> skills;
    private String experienceLevel;
    private String jobType;
    private String workMode;
    private String category;
    private String additionalContext;
}


