package com.sameer.job.dto.ai;

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
public class SkillsGapRequest {
    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private List<String> candidateSkills;
    private List<String> requiredSkills;
}
