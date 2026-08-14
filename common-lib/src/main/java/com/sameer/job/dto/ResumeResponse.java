package com.sameer.job.dto;

import com.sameer.job.domain.ResumeTemplate;
import com.sameer.job.domain.ResumeVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {

    private Long id;
    private Long candidateId;
    private String title;
    private ResumeTemplate template;
    private ResumeVisibility visibility;
    private Boolean isDefault;
    private PersonalInfoResponse personalInfoResponse;
    private String summary;
    private Integer completionScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

//    TODO:
    //    private List<WorkExperienceResponse> workExperiences;
    //    private List<EducationResponse> educations;
    //    private List<ResumeSkillResponse> skills;
    //    private List<ProjectResponse> projects;
    //    private List<CertificationResponse> certifications;
    //    private List<AwardResponse> awards;
    //    private List<LanguageResponse> languages;
}