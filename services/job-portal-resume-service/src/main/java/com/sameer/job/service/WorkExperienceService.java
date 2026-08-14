package com.sameer.job.service;

import com.sameer.job.dto.WorkExperienceResponse;
import com.sameer.job.modal.WorkExperience;
import com.sameer.job.payload.AddWorkExperience;

import java.util.List;

public interface WorkExperienceService {
    WorkExperienceResponse addWorkExperience(Long resumeId, Long candidateId, AddWorkExperience req) throws Exception;

    List<WorkExperienceResponse> getWorkExperiences(Long resumeId);

    WorkExperienceResponse updateWorkExperience(
            Long resumeId, Long workExperienceId, Long candidateId, AddWorkExperience req
    ) throws Exception;

    void deleteWorkExperience(Long resumeId, Long workExperienceId, Long candidateId) throws Exception;

    WorkExperience getWorkExperienceEntity(Long workExperienceId) throws Exception;
}
