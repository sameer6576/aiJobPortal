package com.sameer.job.service;

import com.sameer.job.dto.JobRequest;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.payload.JobSearchRequest;

import java.util.List;

public interface JobService {

     JobResponse createJob(Long employerId, JobRequest req) throws Exception;

    JobResponse getJobById(Long id) throws Exception;

    List<JobResponse> getJobs(JobSearchRequest jobSearchRequest);

    List<JobResponse> getJobsByCompany(Long companyId);

    JobResponse updateJob(Long jobId, Long employerId, JobRequest req) throws Exception;

    JobResponse publishJob(Long jobId, Long employerId) throws Exception;

    JobResponse closeJob(Long jobId, Long employerId) throws Exception;

    void deleteJob(Long jobId, Long employerId) throws Exception;

    List<JobResponse> getAllJobsAdmin();

}
