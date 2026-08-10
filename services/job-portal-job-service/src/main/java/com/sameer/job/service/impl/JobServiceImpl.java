package com.sameer.job.service.impl;

import com.sameer.job.domain.JobStatus;
import com.sameer.job.dto.JobRequest;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.mapper.JobMapper;
import com.sameer.job.modal.Job;
import com.sameer.job.modal.embeddable.JobLocation;
import com.sameer.job.modal.embeddable.SalaryRange;
import com.sameer.job.payload.JobSearchRequest;
import com.sameer.job.repository.JobRepository;
import com.sameer.job.repository.JobSpecification;
import com.sameer.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;


    @Override
    public JobResponse createJob(Long employerId, JobRequest req) {
        // TODO: fetch company by employerId

        Long companyId = 1L;

        Job job = Job.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .requirements(req.getRequirements())
                .responsibilities(req.getResponsibilities())
                .benefits(req.getBenefits())
                .companyId(companyId)
                .employerId(employerId)
//                .category(category)
//                .skills(skills)
//                .tags(tags)
                .location(buildLocation(req))
                .salaryRange(buildSalaryRange(req))
                .jobType(req.getJobType())
                .workMode(req.getWorkMode())
                .experienceLevel(req.getExperienceLevel())
                .opening(req.getOpenings() != null ? req.getOpenings() : 1)
                .applicationDeadline(req.getApplicationDeadline())
                .expiresAt(req.getExpiresAt())
                .build();

        Job savedJob = jobRepository.save(job);

        return convertToResponse(savedJob);
    }

    @Override
    public JobResponse getJobById(Long id) throws Exception {
        Job job = jobRepository.findById(id).orElseThrow(
                () -> new Exception("Job not found with ID: " + id)
        );
        return convertToResponse(job);
    }

    @Override
    public List<JobResponse> getJobs(JobSearchRequest jobSearchRequest) {
        List<Job> jobs = jobRepository.findAll(JobSpecification.build(jobSearchRequest));

        return jobs.stream().map(
                this::convertToResponse
        ).collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByCompany(Long companyId) {
        List<Job> jobs = jobRepository.findByCompanyId(companyId);

        return jobs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public JobResponse updateJob(Long jobId, Long employerId, JobRequest req) throws Exception {
        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new Exception("Job not found with ID: " + jobId)
        );
        assertEmployer(job,employerId);

        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setRequirements(req.getRequirements());
        job.setResponsibilities(req.getResponsibilities());
        job.setBenefits(req.getBenefits());
        // TODO: not implemented yet
//        job.setCategory(category);
//        job.setSkills(skills);
//        job.setTags(tags);

        job.setLocation(buildLocation(req));

        job.setSalaryRange(buildSalaryRange(req));

        job.setJobType(req.getJobType());
        job.setWorkMode(req.getWorkMode());
        job.setExperienceLevel(req.getExperienceLevel());

        job.setOpening(req.getOpenings() != null ? req.getOpenings(): job.getOpening());

        job.setApplicationDeadline(req.getApplicationDeadline());
        job.setExpiresAt(req.getExpiresAt());

        Job updated = jobRepository.save(job);

        return convertToResponse(updated);
    }

    @Override
    public JobResponse publishJob(Long jobId, Long employerId) throws Exception {
        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new Exception("Job not found with ID: " + jobId)
        );
        assertEmployer(job,employerId);

        if(job.getStatus() == JobStatus.CLOSED || job.getStatus() ==JobStatus.EXPIRED){
            throw new Exception("Job is already closed/expired");
        }

        job.setStatus(JobStatus.OPEN);
        job.setPublishedAt(LocalDateTime.now());
        job.setActive(true);

        Job savedJob = jobRepository.save(job);
        return convertToResponse(savedJob);

    }

    @Override
    public JobResponse closeJob(Long jobId, Long employerId) throws Exception {
        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new Exception("Job not found with ID: " + jobId)
        );
        assertEmployer(job,employerId);

        job.setStatus(JobStatus.CLOSED);
        job.setClosedAt(LocalDateTime.now());
        job.setActive(false);

        Job savedJob = jobRepository.save(job);
        return convertToResponse(savedJob);

    }

    @Override
    public void deleteJob(Long jobId, Long employerId) throws Exception {
        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new Exception("Job not found with ID: " + jobId)
        );
        assertEmployer(job,employerId);

       jobRepository.delete(job);
    }

    @Override
    public List<JobResponse> getAllJobsAdmin() {
        return jobRepository.findAll().stream().map(
                this::convertToResponse
        ).collect(Collectors.toList());
    }


    private JobResponse convertToResponse(Job savedJob) {
        // TODO: fetch company response
        CompanyResponse companyResponse = CompanyResponse.builder().
                id(savedJob.getId())
                .build();

        return JobMapper.toResponse(savedJob, companyResponse);
    }

    private SalaryRange buildSalaryRange(JobRequest req) {
        return SalaryRange.builder()
                .minSalary(req.getMinSalary())
                .maxSalary(req.getMaxSalary())
                .build();
    }

    private JobLocation buildLocation(JobRequest req) {
        return JobLocation.builder()
                .address(req.getAddress())
                .city(req.getCity())
                .state(req.getState())
                .country(req.getCountry())
                .zipCode(req.getZipCode())
                .build();
    }

    private void assertEmployer(Job job, Long employerId) throws Exception {
        if(!job.getEmployerId().equals(employerId)){
            throw new Exception("You are not the employer who posted this job: "+job.getId());
        }
    }
}
