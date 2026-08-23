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
public class SalaryRangeRequest {

    @NotBlank(message = "Job title is required")
    private String title;
    private List<String> skills;
    private String experienceLevel;
    private String jobType;
    private String location;
}
