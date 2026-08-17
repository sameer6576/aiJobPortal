package com.sameer.job.service;

import com.sameer.job.domain.ApplicationStatus;
import com.sameer.job.dto.ApplicationResponse;
import com.sameer.job.modal.Application;
import com.sameer.job.payload.CompanyApplicationFilterRequest;
import com.sameer.job.payload.CreateApplicationRequest;
import com.sameer.job.payload.WithdrawApplicationRequest;

import java.util.List;

public interface ApplicationService {
    ApplicationResponse createApplication(
            Long candidateId,
            CreateApplicationRequest req
    ) throws Exception;

    ApplicationResponse getApplicationById(Long id) throws Exception;
    List<ApplicationResponse> getMyApplications(Long candidateId);
    List<ApplicationResponse> getApplicationsForCompany(Long userId, CompanyApplicationFilterRequest filter);
    List<ApplicationResponse> getApplicationsForJob(Long jobId);
    ApplicationResponse updateStatus(Long applicationId, Long employerId, ApplicationStatus status) throws Exception;
    ApplicationResponse withdraw(Long applicationId, Long candidateId, WithdrawApplicationRequest req) throws Exception;
    ApplicationResponse toggleStar(Long applicationId, Long employerId) throws Exception;
    void deleteApplication(Long applicationId, Long candidateId) throws Exception;
    Application getApplicationEntity(Long applicationId) throws Exception;




}
