package com.sameer.job.service;

import com.sameer.job.dto.EducationResponse;
import com.sameer.job.payload.AddEducationRequest;

import java.util.List;

public interface EducationService {

    EducationResponse addEducation(Long resumeId, Long candidateId, AddEducationRequest request) throws Exception;

    List<EducationResponse> getEducations(Long resumeId, Long candidateId) throws Exception;

    EducationResponse updateEducation(Long educationId, Long resumeId, Long candidateId, AddEducationRequest request) throws Exception;

    void deleteEducation(Long educationId, Long resumeId, Long candidateId) throws Exception;
}
