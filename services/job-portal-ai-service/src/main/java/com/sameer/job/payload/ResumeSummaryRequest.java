package com.sameer.job.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeSummaryRequest {
    private String targetJobTitle;

    private List<WorkExperienceInfo> workExperiences;
    private List<String> skills;
    private List<EducationInfo> educations;
    private Integer yearsOfExperience;

    private String additionalContext;


    @Data
    public static class WorkExperienceInfo {
        private String jobTitle;
        private String companyName;
        private String description;
    }

    @Data
    public static class EducationInfo {
        private String degree;
        private String fieldOfStudy;
        private String institutionName;
    }
}
