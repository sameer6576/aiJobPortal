package com.sameer.job.mapper;

import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.JobSkillResponse;
import com.sameer.job.dto.JobTagResponse;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.modal.Job;
import com.sameer.job.modal.embeddable.JobLocation;
import com.sameer.job.modal.embeddable.SalaryRange;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class JobMapper {

    public static JobResponse toResponse(Job job, CompanyResponse companyResponse) {

        JobLocation jobLocation = job.getLocation();
        SalaryRange salaryRange = job.getSalaryRange();

        Set<JobSkillResponse> skills = job.getSkills() == null ?
                Collections.emptySet()
                : job.getSkills().stream().map(JobSkillMapper::toJobSkillResponse)
                     .collect(Collectors.toSet());

        Set<JobTagResponse> tags = job.getTags() == null ?
                Collections.emptySet()
                : job.getTags().stream().map(JobTagMapper::toJobTagResponse)
                     .collect(Collectors.toSet());

        return JobResponse.builder()
                          .id(job.getId())
                          .title(job.getTitle())
                          .description(job.getDescription())
                          .requirements(job.getRequirements())
                          .responsibilities(job.getResponsibilities())
                          .benefits(job.getBenefits())
                          .company(companyResponse)
                          .category(JobCategoryMapper.toJobCategoryResponse(job.getCategory(), false))
                          .skills(skills)
                          .tags(tags)
                          .employerId(job.getEmployerId())

                          .address(jobLocation != null ? jobLocation.getAddress() : null)
                          .city(jobLocation != null ? jobLocation.getCity() : null)
                          .state(jobLocation != null ? jobLocation.getState() : null)
                          .country(jobLocation != null ? jobLocation.getCountry() : null)
                          .zipCode(jobLocation != null ? jobLocation.getZipCode() : null)

                          .minSalary(salaryRange != null ? salaryRange.getMinSalary() : null)
                          .maxSalary(salaryRange != null ? salaryRange.getMaxSalary() : null)

                          .jobType(job.getJobType())
                          .workMode(job.getWorkMode())
                          .experienceLevel(job.getExperienceLevel())
                          .status(job.getStatus())

                          .openings(job.getOpening())
                          .applicationDeadline(job.getApplicationDeadline())
                          .expiresAt(job.getExpiresAt())
                          .active(job.getActive())

                          .createdAt(job.getCreatedAt())
                          .updatedAt(job.getUpdatedAt())
                          .publishedAt(job.getPublishedAt())
                          .closedAt(job.getClosedAt())
                          .build();
    }
}
