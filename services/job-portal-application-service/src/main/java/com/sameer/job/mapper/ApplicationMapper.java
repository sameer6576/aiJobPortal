package com.sameer.job.mapper;

import com.sameer.job.domain.AiShortListStatus;
import com.sameer.job.domain.ApplicationStatus;
import com.sameer.job.dto.ApplicationNoteResponse;
import com.sameer.job.dto.ApplicationResponse;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.dto.response.UserResponse;
import com.sameer.job.modal.Application;
import com.sameer.job.modal.ApplicationNote;
import com.sameer.job.payload.CreateApplicationRequest;

import java.util.List;

public class ApplicationMapper {
    public static Application toEntity(CreateApplicationRequest req, Long candidateId, Long companyId, Long employerId) {
        if (req == null) return null;

        return Application.builder()
                          .candidateId(candidateId)
                          .jobId(req.getJobId())
                          .companyId(companyId)
                          .employerId(employerId)
                          .resumeId(req.getResumeId())
                          .coverLetter(req.getCoverLetter())
                          .expectedSalary(req.getExpectedSalary())
                          .availableFrom(req.getAvailableFrom())
                          .status(ApplicationStatus.PENDING)
                          .aiShortListStatus(AiShortListStatus.NOT_SCREENED)
                          .build();
    }

    public static ApplicationResponse toResponse(
            Application application,
            List<ApplicationNote> notes,
            JobResponse job,
            CompanyResponse company,
            UserResponse candidate
    ) {
        if (application == null) return null;

        return ApplicationResponse.builder().id(application.getId())
                                  .candidate(candidate)
                                  .employerId(application.getEmployerId())
                                  .job(job)
                                  .company(company)
                                  .status(application.getStatus())
                                  .resumeId(application.getResumeId())
                                  .coverLetter(application.getCoverLetter())
                                  .expectedSalary(application.getExpectedSalary())
                                  .availableFrom(application.getAvailableFrom())
                                  .isStarred(application.getIsStarred())
                                  .aiScore(application.getAiScore())
                                  .aiShortListStatus(application.getAiShortListStatus())
                                  .notes(notes.stream().map(ApplicationMapper::toNoteResponse).toList())
                                  .withdrawnAt(application.getWithdrawnAt())
                                  .withdrawnReason(application.getWithdrawnReason())
                                  .appliedAt(application.getAppliedAt())
                                  .updatedAt(application.getUpdatedAt())
                                  .build();
    }

    public static ApplicationNoteResponse toNoteResponse(ApplicationNote note) {
        return ApplicationNoteResponse.builder()
                                      .id(note.getId())
                                      .addedByUserId(note.getAddedByUserId())
                                      .content(note.getContent())
                                      .createdAt(note.getCreatedAt())
                                      .build();
    }
}
