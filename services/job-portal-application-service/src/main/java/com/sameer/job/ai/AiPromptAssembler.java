package com.sameer.job.ai;

import com.sameer.job.domain.AiShortListStatus;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.JobSkillResponse;
import com.sameer.job.dto.PersonalInfoResponse;
import com.sameer.job.dto.ResumeResponse;
import com.sameer.job.dto.ResumeSkillResponse;
import com.sameer.job.dto.WorkExperienceResponse;
import com.sameer.job.dto.ai.CoverLetterRequest;
import com.sameer.job.dto.ai.ScreeningScoreRequest;
import com.sameer.job.dto.ai.SkillsGapRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiPromptAssembler {

    public ScreeningScoreRequest screeningRequest(JobResponse job, ResumeResponse resume) {
        return ScreeningScoreRequest.builder()
                                     .jobTitle(job.getTitle())
                                     .experienceLevel(job.getExperienceLevel() != null ? job.getExperienceLevel().name() : null)
                                     .requiredSkills(jobSkillNames(job))
                                     .responsibilities(job.getResponsibilities())
                                     .candidateSummary(resume.getSummary())
                                     .candidateSkills(resumeSkillNames(resume))
                                     .candidateExperience(experienceLines(resume))
                                     .build();
    }

    public CoverLetterRequest coverLetterRequest(JobResponse job, ResumeResponse resume, String candidateName) {
        String companyName = job.getCompany() != null ? job.getCompany().getName() : null;
        return CoverLetterRequest.builder()
                                  .jobTitle(job.getTitle())
                                  .jobDescription(job.getDescription())
                                  .candidateName(candidateName)
                                  .candidateSummary(resume.getSummary())
                                  .candidateSkills(resumeSkillNames(resume))
                                  .candidateExperience(experienceLines(resume))
                                  .targetCompanyName(companyName)
                                  .build();
    }

    public SkillsGapRequest skillsGapRequest(JobResponse job, ResumeResponse resume) {
        return SkillsGapRequest.builder()
                                  .jobTitle(job.getTitle())
                                  .candidateSkills(resumeSkillNames(resume))
                                  .requiredSkills(jobSkillNames(job))
                                  .build();
    }

    public String candidateName(ResumeResponse resume, String fallback) {
        PersonalInfoResponse info = resume.getPersonalInfoResponse();
        if (info == null) {
            return fallback;
        }
        String first = info.getFirstName() != null ? info.getFirstName() : "";
        String last = info.getLastName() != null ? info.getLastName() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? fallback : full;
    }

    public AiShortListStatus shortListStatus(int score) {
        if (score >= 80) {
            return AiShortListStatus.AUTO_SHORTLISTED;
        }
        if (score >= 50) {
            return AiShortListStatus.REVIEW_RECOMMENDED;
        }
        return AiShortListStatus.LOW_MATCH;
    }

    private List<String> jobSkillNames(JobResponse job) {
        if (job.getSkills() == null) {
            return List.of();
        }
        return job.getSkills().stream()
                    .map(JobSkillResponse::getName)
                    .filter(name -> name != null && !name.isBlank())
                    .toList();
    }

    private List<String> resumeSkillNames(ResumeResponse resume) {
        if (resume.getSkills() == null) {
            return List.of();
        }
        return resume.getSkills().stream()
                     .map(ResumeSkillResponse::getSkillName)
                     .filter(name -> name != null && !name.isBlank())
                     .toList();
    }

    private List<String> experienceLines(ResumeResponse resume) {
        if (resume.getWorkExperiences() == null) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        for (WorkExperienceResponse experience : resume.getWorkExperiences()) {
            String title = experience.getJobTitle() != null ? experience.getJobTitle() : "";
            String company = experience.getCompanyName() != null ? experience.getCompanyName() : "";
            String line = (title + " at " + company).trim();
            if (!line.isBlank() && !"at".equals(line)) {
                lines.add(line);
            }
        }
        return lines;
    }
}
