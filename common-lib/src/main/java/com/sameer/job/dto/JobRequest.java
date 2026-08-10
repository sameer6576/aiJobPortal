package com.sameer.job.dto;

import com.sameer.job.domain.ExperienceLevel;
import com.sameer.job.domain.JobType;
import com.sameer.job.domain.WorkMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String requirements;
    private String responsibilities;
    private String benefits;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Set<Long> skillIds;
    private Set<Long> tagIds;

    // Location
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    // Salary
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum salary must not be negative")
    private BigDecimal minSalary;

    @DecimalMin(value = "0.0", inclusive = true, message = "Maximum salary must not be negative")
    private BigDecimal maxSalary;


    // Classification

    @NotNull(message = "Job type is required")
    private JobType jobType;
    @NotNull(message = "Work mode is required")
    private WorkMode workMode;
    @NotNull(message = "Experience is required")
    private ExperienceLevel experienceLevel;

//    @NotNull(message = "Job type is required")
//    private JobStatus status;

    // Posting Details
    @Min(value = 1, message = "Openings must be at least 1")
    private Integer openings=1;

    private LocalDate applicationDeadline;
    private LocalDate expiresAt;
}
