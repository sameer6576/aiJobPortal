package com.sameer.job.mapper;

import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.modal.Job;
import com.sameer.job.modal.embeddable.JobLocation;
import com.sameer.job.modal.embeddable.SalaryRange;

import java.time.LocalDateTime;

public class JobMapper {

    public static JobResponse toResponse(Job job, CompanyResponse companyResponse){

        JobLocation jobLocation = job.getLocation();
        SalaryRange salaryRange = job.getSalaryRange();

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .responsibilities(job.getResponsibilities())
                .benefits(job.getBenefits())
                .company(companyResponse)

//                .category(toCategoryResponse(job.getCategory))
//                .skills(skills)
//                .tags(tags)

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
