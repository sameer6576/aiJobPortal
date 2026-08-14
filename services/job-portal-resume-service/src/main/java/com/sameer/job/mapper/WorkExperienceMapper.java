package com.sameer.job.mapper;

import com.sameer.job.dto.WorkExperienceResponse;
import com.sameer.job.modal.WorkExperience;

public class WorkExperienceMapper {
    public static WorkExperienceResponse toWorkExperienceResponse(WorkExperience workExperience) {
        if (workExperience == null) return null;

        return WorkExperienceResponse.builder()
                                     .id(workExperience.getId()).companyName(workExperience.getCompanyName())
                                     .companyLogoUrl(workExperience.getCompanyLogoUrl())
                                     .jobTitle(workExperience.getJobTitle())
                                     .employmentType(workExperience.getEmploymentType())
                                     .location(workExperience.getLocation()).startDate(workExperience.getStartDate())
                                     .endDate(workExperience.getEndDate())
                                     .isCurrentJob(workExperience.getIsCurrentJob())
                                     .technologies(workExperience.getTechnologies())
                                     .displayOrder(workExperience.getDisplayOrder())
                                     .build();
    }
}
