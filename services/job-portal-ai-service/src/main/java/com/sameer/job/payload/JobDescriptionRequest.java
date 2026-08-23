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


