package com.sameer.job.mapper;

import com.sameer.job.dto.*;
import com.sameer.job.modal.*;

public final class ResumeMapper {

    public static PersonalInfoResponse toPersonalInfoResponse(PersonalInfo personalInfo) {

        if (personalInfo == null) {
            return null;
        }

        return PersonalInfoResponse.builder()
                                   .firstName(personalInfo.getFirstName())
                                   .lastName(personalInfo.getLastName())
                                   .headline(personalInfo.getHeadline())
                                   .email(personalInfo.getEmail())
                                   .phone(personalInfo.getPhone())
                                   .city(personalInfo.getCity())
                                   .country(personalInfo.getCountry())
                                   .linkedinUrl(personalInfo.getLinkedinUrl())
                                   .githubUrl(personalInfo.getGithubUrl())
                                   .portfolioUrl(personalInfo.getPortfolioUrl())
                                   .websiteUrl(personalInfo.getWebsiteUrl())
                                   .build();
    }


    public static ResumeResponse toPersonalInfoResponse(Resume resume) {

        if (resume == null) return null;

        return ResumeResponse.builder()
                             .id(resume.getId())
                             .candidateId(resume.getCandidateId())
                             .title(resume.getTitle())
                             .template(resume.getTemplate())
                             .visibility(resume.getVisibility())
                             .isDefault(resume.getIsDefault())
                             .personalInfoResponse(toPersonalInfoResponse(resume.getPersonalInfo()))
                             .summary(resume.getSummary())
                             .completionScore(resume.getCompletionScore())
                             .createdAt(resume.getCreatedAt())
                             .updatedAt(resume.getUpdatedAt())
                             .build();
    }

    public static ResumeSkillResponse toSkillResponse(ResumeSkill skill) {

        if (skill == null) return null;

        return ResumeSkillResponse.builder()
                                  .id(skill.getId())
                                  .skillName(skill.getSkillName())
                                  .proficiencyLevel(skill.getProficiencyLevel())
                                  .yearsOfExperience(skill.getYearsOfExperience())
                                  .displayOrder(skill.getDisplayOrder())
                                  .build();
    }

    public static EducationResponse toEducationResponse(Education education) {

        if (education == null) {
            return null;
        }

        return EducationResponse.builder()
                                .id(education.getId())
                                .institutionName(education.getInstitutionName())
                                .degree(education.getDegree())
                                .fieldOfStudy(education.getFieldOfStudy())
                                .grade(education.getGrade())
                                .startDate(education.getStartDate())
                                .endDate(education.getEndDate())
                                .isCurrentlyStudying(education.getIsCurrentlyStudying())
                                .description(education.getDescription())
                                .displayOrder(education.getDisplayOrder())
                                .build();
    }

    public static ProjectResponse toProjectResponse(Project project) {

        if (project == null) {
            return null;
        }

        return ProjectResponse.builder()
                              .id(project.getId())
                              .title(project.getTitle())
                              .description(project.getDescription())
                              .technologies(project.getTechnologies())
                              .projectUrl(project.getProjectUrl())
                              .sourceCodeUrl(project.getSourceCodeUrl())
                              .startDate(project.getStartDate())
                              .endDate(project.getEndDate())
                              .isOngoing(project.getIsOngoing())
                              .displayOrder(project.getDisplayOrder())
                              .build();
    }
}
