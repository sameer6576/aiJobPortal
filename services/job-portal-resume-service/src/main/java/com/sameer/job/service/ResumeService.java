package com.sameer.job.service;

import com.sameer.job.dto.PersonalInfoResponse;
import com.sameer.job.dto.ResumeResponse;
import com.sameer.job.modal.Resume;
import com.sameer.job.payload.CreateResumeRequest;

import java.util.List;

public interface ResumeService {
    ResumeResponse createResume(Long candidateId, CreateResumeRequest req);

    ResumeResponse getResumeById(Long resumeId, Long candidateId) throws Exception;

    List<ResumeResponse> getMyResumes(Long candidateId);

    ResumeResponse updatePersonalInfo(Long resumeId, Long candidateId, PersonalInfoResponse req) throws Exception;

    ResumeResponse updateSummary(Long resumeId, Long candidateId, String summary) throws Exception;

    ResumeResponse setDefaultResume(Long resumeId, Long candidateId) throws Exception;

    void deleteResume(Long resumeId, Long candidateId) throws Exception;

    Resume getResumeEntity(Long resumeId) throws Exception;
}
