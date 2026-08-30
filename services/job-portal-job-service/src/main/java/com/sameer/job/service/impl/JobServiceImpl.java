package com.sameer.job.service.impl;

import com.sameer.job.client.CompanyClient;
import com.sameer.job.client.JobAiClient;
import com.sameer.job.domain.JobStatus;
import com.sameer.job.dto.JobRequest;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.exception.ConflictException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.JobMapper;
import com.sameer.job.modal.Job;
import com.sameer.job.modal.JobCategory;
import com.sameer.job.modal.JobSkill;
import com.sameer.job.modal.JobTag;
import com.sameer.job.modal.embeddable.JobLocation;
import com.sameer.job.modal.embeddable.SalaryRange;
import com.sameer.job.dto.ai.SearchEnhanceRequest;
import com.sameer.job.payload.JobSearchRequest;
import com.sameer.job.payload.NaturalLanguageSearchMapper;
import com.sameer.job.repository.JobRepository;
import com.sameer.job.repository.JobSpecification;
import com.sameer.job.service.JobCategoryService;
import com.sameer.job.service.JobService;
import com.sameer.job.service.JobSkillService;
import com.sameer.job.service.JobTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobCategoryService jobCategoryService;
    private final JobSkillService jobSkillService;
    private final JobTagService jobTagService;
    private final CompanyClient companyClient;
    private final JobAiClient jobAiClient;


    @Override
    public JobResponse createJob(Long employerId, JobRequest req) throws Exception {

        JobCategory category = jobCategoryService.getCategoryEntityById(req.getCategoryId());

        Set<JobSkill> skills = req.getSkillIds() != null ?
                jobSkillService.getSkillByIds(req.getSkillIds())
                : Collections.emptySet();

        Set<JobTag> tags = req.getTagIds() != null ?
                jobTagService.getTagsByIds(req.getTagIds()) : Collections.emptySet();

        CompanyResponse companyResponse = companyClient.getMyCompany(employerId);

        Long companyId = companyResponse.getId();

        Job job = Job.builder()
                     .title(req.getTitle())
                     .description(req.getDescription())
                     .requirements(req.getRequirements())
                     .responsibilities(req.getResponsibilities())
                     .benefits(req.getBenefits())
                     .companyId(companyId)
                     .employerId(employerId)
                     .category(category)
                     .skills(skills)
                     .tags(tags)
                     .location(buildLocation(req))
                     .salaryRange(buildSalaryRange(req))
                     .jobType(req.getJobType())
                     .workMode(req.getWorkMode())
                     .experienceLevel(req.getExperienceLevel())
                     .opening(req.getOpenings() != null ? req.getOpenings() : 1)
                     .applicationDeadline(req.getApplicationDeadline())
                     .expiresAt(req.getExpiresAt())
                     .active(true)
                     .status(JobStatus.DRAFT)
                     .build();

        Job savedJob = jobRepository.save(job);

        return convertToResponse(savedJob);
    }

    @Override
    public JobResponse getJobById(Long id, Long userId, String role) throws Exception {
        Job job = jobRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Job not found with ID: " + id)
        );
        if (job.getStatus() == JobStatus.DRAFT && !canViewDraft(job, userId, role)) {
            throw new NotFoundException("Job not found with ID: " + id);
        }
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
    public List<JobResponse> searchByNaturalLanguage(String query) throws Exception {
        JobSearchRequest mapped;
        try {
            SearchEnhanceRequest request = SearchEnhanceRequest.builder().query(query).build();
            mapped = NaturalLanguageSearchMapper.toJobSearchRequest(
                    jobAiClient.enhanceSearch(request)
            );
        } catch (Exception e) {
            log.error("Natural language search enhancement failed", e);
            mapped = new JobSearchRequest();
            mapped.setKeyword(query);
        }
        return getJobs(mapped);
    }

    @Override
    public List<JobResponse> getJobsByCompany(Long companyId) {
        List<Job> jobs = jobRepository.findByCompanyId(companyId);

        return jobs.stream()
                   .filter(job -> job.getStatus() == JobStatus.OPEN)
                   .map(this::convertToResponse)
                   .collect(Collectors.toList());
    }

    @Override
    public JobResponse updateJob(Long jobId, Long employerId, JobRequest req) throws Exception {
        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new NotFoundException("Job not found with ID: " + jobId)
        );
        assertEmployer(job, employerId);

        JobCategory category = jobCategoryService.getCategoryEntityById(req.getCategoryId());

        Set<JobSkill> skills = req.getSkillIds() != null ?
                jobSkillService.getSkillByIds(req.getSkillIds())
                : Collections.emptySet();

        Set<JobTag> tags = req.getTagIds() != null ?
                jobTagService.getTagsByIds(req.getTagIds()) : Collections.emptySet();

        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setRequirements(req.getRequirements());
        job.setResponsibilities(req.getResponsibilities());
        job.setBenefits(req.getBenefits());
        job.setCategory(category);
        job.setSkills(skills);
        job.setTags(tags);

        job.setLocation(buildLocation(req));

        job.setSalaryRange(buildSalaryRange(req));

        job.setJobType(req.getJobType());
        job.setWorkMode(req.getWorkMode());
        job.setExperienceLevel(req.getExperienceLevel());

        job.setOpening(req.getOpenings() != null ? req.getOpenings() : job.getOpening());

        job.setApplicationDeadline(req.getApplicationDeadline());
        job.setExpiresAt(req.getExpiresAt());

        Job updated = jobRepository.save(job);

        return convertToResponse(updated);
    }

    @Override
    public JobResponse publishJob(Long jobId, Long employerId) throws Exception {
        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new NotFoundException("Job not found with ID: " + jobId)
        );
        assertEmployer(job, employerId);

        if (job.getStatus() == JobStatus.CLOSED || job.getStatus() == JobStatus.EXPIRED) {
            throw new ConflictException("Job is already closed/expired");
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
                () -> new NotFoundException("Job not found with ID: " + jobId)
        );
        assertEmployer(job, employerId);

        job.setStatus(JobStatus.CLOSED);
        job.setClosedAt(LocalDateTime.now());
        job.setActive(false);

        Job savedJob = jobRepository.save(job);
        return convertToResponse(savedJob);

    }

    @Override
    public void deleteJob(Long jobId, Long employerId) throws Exception {
        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new NotFoundException("Job not found with ID: " + jobId)
        );
        assertEmployer(job, employerId);

        jobRepository.delete(job);
    }

    @Override
    public List<JobResponse> getAllJobsAdmin() {
        return jobRepository.findAll().stream().map(
                this::convertToResponse
        ).collect(Collectors.toList());
    }


    private JobResponse convertToResponse(Job savedJob) {
        CompanyResponse companyResponse = companyClient.getCompanyById(savedJob.getCompanyId());

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
        if (!job.getEmployerId().equals(employerId)) {
            throw new com.sameer.job.exception.ForbiddenException("You are not the employer who posted this job: " + job.getId());
        }
    }

    private boolean canViewDraft(Job job, Long userId, String role) {
        boolean owner = userId != null && userId.equals(job.getEmployerId());
        boolean admin = role != null && role.contains("ROLE_ADMIN");
        return owner || admin;
    }
}
