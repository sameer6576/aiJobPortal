package com.sameer.job.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperienceBulletRequest {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String company;

    @NotBlank(message = "Raw description is required")
    private String rawDescription;

    private String achievementsHint;
}
