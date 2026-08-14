package com.sameer.job.mapper;

import com.sameer.job.dto.PersonalInfoResponse;
import com.sameer.job.dto.ResumeResponse;
import com.sameer.job.modal.PersonalInfo;
import com.sameer.job.modal.Resume;

public final class ResumeMapper {

    public static PersonalInfoResponse toResponse(PersonalInfo personalInfo) {

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


    public static ResumeResponse toResponse(Resume resume) {

        if(resume == null) return null;

        return ResumeResponse.builder()
                             .id(resume.getId())
                             .candidateId(resume.getCandidateId())
                             .title(resume.getTitle())
                             .template(resume.getTemplate())
                             .visibility(resume.getVisibility())
                             .isDefault(resume.getIsDefault())
                             .personalInfoResponse(toResponse(resume.getPersonalInfo()))
                             .summary(resume.getSummary())
                             .completionScore(resume.getCompletionScore())
                             .createdAt(resume.getCreatedAt())
                             .updatedAt(resume.getUpdatedAt())
                             .build();
    }
}