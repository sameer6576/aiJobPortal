package com.sameer.job.dto;

import com.sameer.job.domain.ExperienceLevel;
import com.sameer.job.domain.JobStatus;
import com.sameer.job.domain.JobType;
import com.sameer.job.domain.WorkMode;
import com.sameer.job.dto.response.CompanyResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String responsibilities;
    private String benefits;

    private CompanyResponse company;
    private Long employerId;

    private JobCategoryResponse category;
    private Set<JobSkillResponse> skills;
    private Set<JobTagResponse> tags;

    // Location
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    // Salary
    private BigDecimal minSalary;
    private BigDecimal maxSalary;

    // Classification
    private JobType jobType;
    private WorkMode workMode;
    private ExperienceLevel experienceLevel;
    private JobStatus status;

    // Posting Details
    private Integer openings;
    private LocalDate applicationDeadline;
    private LocalDate expiresAt;
    private Boolean active;
    // Analytics
    private Long viewCount;
    private Long applicationCount;
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;


}
