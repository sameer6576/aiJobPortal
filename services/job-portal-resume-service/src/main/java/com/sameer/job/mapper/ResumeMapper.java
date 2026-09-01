package com.sameer.job.mapper;

import com.sameer.job.dto.*;
import com.sameer.job.modal.*;

import java.util.ArrayList;
import java.util.List;

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


    public static ResumeResponse toResponse(Resume resume,
                                            List<WorkExperienceResponse> workExperiences,
                                            List<EducationResponse> educations,
                                            List<ResumeSkillResponse> skills,
                                            List<ProjectResponse> projects,
                                            List<LanguageResponse> languages,
                                            List<AwardResponse> awards,
                                            List<CertificationResponse> certifications) {

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
                             .workExperiences(workExperiences)
                             .educations(educations)
                             .languages(languages)
                             .projects(projects)
                             .skills(skills)
                             .awards(awards)
                             .certifications(certifications)
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
                              .technologies(
                                      project.getTechnologies() != null
                                              ? new ArrayList<>(project.getTechnologies())
                                              : new ArrayList<>()
                              )
                              .projectUrl(project.getProjectUrl())
                              .sourceCodeUrl(project.getSourceCodeUrl())
                              .startDate(project.getStartDate())
                              .endDate(project.getEndDate())
                              .isOngoing(project.getIsOngoing())
                              .displayOrder(project.getDisplayOrder())
                              .build();
    }

    public static LanguageResponse toLanguageResponse(Language language) {

        if (language == null) {
            return null;
        }

        return LanguageResponse.builder()
                               .id(language.getId())
                               .languageName(language.getLanguageName())
                               .proficiency(language.getProficiency())
                              .displayOrder(language.getDisplayOrder())
                              .build();
    }

    public static AwardResponse toAwardResponse(Award award) {

        if (award == null) {
            return null;
        }

        return AwardResponse.builder()
                            .id(award.getId())
                            .title(award.getTitle())
                            .issuedBy(award.getIssuedBy())
                            .awardDate(award.getAwardDate())
                            .description(award.getDescription())
                            .displayOrder(award.getDisplayOrder())
                            .build();
    }

    public static CertificationResponse toCertificationResponse(Certification certification) {

        if (certification == null) {
            return null;
        }

        return CertificationResponse.builder()
                                    .id(certification.getId())
                                    .name(certification.getName())
                                    .issuingOrganization(certification.getIssuingOrganization())
                                    .issueDate(certification.getIssueDate())
                                    .expiryDate(certification.getExpiryDate())
                                    .credentialId(certification.getCredentialId())
                                    .credentialUrl(certification.getCredentialUrl())
                                    .displayOrder(certification.getDisplayOrder())
                                    .build();
    }
}
