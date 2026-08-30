package com.sameer.job.service.impl;

import com.sameer.job.ai.AiPromptAssembler;
import com.sameer.job.client.AiClient;
import com.sameer.job.client.CompanyClient;
import com.sameer.job.client.JobClient;
import com.sameer.job.client.ResumeClient;
import com.sameer.job.client.UserClient;
import com.sameer.job.domain.AiShortListStatus;
import com.sameer.job.domain.ApplicationStatus;
import com.sameer.job.domain.JobStatus;
import com.sameer.job.dto.ApplicationResponse;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.ResumeResponse;
import com.sameer.job.dto.ai.AiTextResponse;
import com.sameer.job.dto.ai.ScreeningScoreResponse;
import com.sameer.job.dto.ai.SkillsGapResponse;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.dto.response.UserResponse;
import com.sameer.job.event.ApplicationEventPublisher;
import com.sameer.job.exception.ConflictException;
import com.sameer.job.exception.ErrorCodes;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.ApplicationMapper;
import com.sameer.job.modal.Application;
import com.sameer.job.modal.ApplicationNote;
import com.sameer.job.payload.CompanyApplicationFilterRequest;
import com.sameer.job.payload.CreateApplicationRequest;
import com.sameer.job.payload.WithdrawApplicationRequest;
import com.sameer.job.repository.ApplicationNoteRepository;
import com.sameer.job.repository.ApplicationRepository;
import com.sameer.job.repository.ApplicationSpecification;
import com.sameer.job.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationNoteRepository applicationNoteRepository;
    private final JobClient jobClient;
    private final ResumeClient resumeClient;
    private final CompanyClient companyClient;
    private final UserClient userClient;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AiClient aiClient;
    private final AiPromptAssembler aiPromptAssembler;

    @Override
    public ApplicationResponse createApplication(Long candidateId, CreateApplicationRequest req) throws Exception {
        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, req.getJobId())) {
            throw new ConflictException(ErrorCodes.ALREADY_APPLIED, "You have already applied");
        }

        JobResponse jobResponse = jobClient.getJobById(req.getJobId());
        if (jobResponse.getStatus() != JobStatus.OPEN) {
            throw new ConflictException(ErrorCodes.JOB_NOT_OPEN, "This job is not open for applications");
        }
        Long companyId = jobResponse.getCompany().getId();
        Long employerId = jobResponse.getEmployerId();

        ResumeResponse resumeResponse = resumeClient.getResumeById(req.getResumeId(), candidateId);

        Application application = ApplicationMapper.toEntity(req, candidateId, companyId, employerId);

        Application savedApplication = applicationRepository.save(application);
        applyScreening(savedApplication, jobResponse, resumeResponse);
        Application screened = applicationRepository.save(savedApplication);
        return buildFullResponse(screened);
    }

    @Override
    public ApplicationResponse getApplicationById(Long id, Long userId) throws Exception {
        Application application = getApplicationEntity(id);
        assertViewer(application, userId);
        return buildFullResponse(application);
    }

    @Override
    public List<ApplicationResponse> getMyApplications(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId)
                                    .stream().map(this::buildFullResponse).toList();
    }

    @Override
    public List<ApplicationResponse> getApplicationsForCompany(Long userId, CompanyApplicationFilterRequest filter) {

        Long companyId = companyClient.getMyCompany(userId).getId();
        Sort sort = buildSort(filter.getSortBy());

        return applicationRepository.findAll(
                                            ApplicationSpecification.forCompanyWithFilters(
                                                    companyId, filter.getJobId(),
                                                    filter.getStatus(),
                                                    filter.getIsStarred(),
                                                    filter.getAiShortListStatus(),
                                                    filter.getMinAiScore()
                                            ), sort)
                                    .stream().map(this::buildFullResponse).toList();
    }

    @Override
    public List<ApplicationResponse> getApplicationsForJob(Long jobId, Long userId) throws Exception {
        JobResponse job = jobClient.getJobById(jobId);
        if (job.getEmployerId() == null || !job.getEmployerId().equals(userId)) {
            throw new ForbiddenException("You are not the employer of this job");
        }
        return applicationRepository.findByJobId(jobId)
                                    .stream().map(this::buildFullResponse).toList();
    }

    @Override
    public ApplicationResponse updateStatus(Long applicationId, Long employerId, ApplicationStatus status) throws Exception {
        Application application = getApplicationEntity(applicationId);

        ApplicationStatus oldStatus = application.getStatus();
        assertEmployer(application, employerId);

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new ConflictException("Candidate has already withdrawn");
        }

        application.setStatus(status);
        Application savedApplication = applicationRepository.save(application);

        applicationEventPublisher.publishStatusChange(application, oldStatus, status, "Your application status got changed");

        return buildFullResponse(savedApplication);
    }

    @Override
    public ApplicationResponse withdraw(Long applicationId, Long candidateId, WithdrawApplicationRequest req) throws Exception {
        Application application = getApplicationEntity(applicationId);
        assertCandidate(application, candidateId);

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new ConflictException("Candidate has already withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setWithdrawnReason(req.getReason());
        Application saved = applicationRepository.save(application);
        return buildFullResponse(saved);
    }

    @Override
    public ApplicationResponse toggleStar(Long applicationId, Long employerId) throws Exception {
        Application application = getApplicationEntity(applicationId);
        assertEmployer(application, employerId);
        application.setIsStarred(!application.getIsStarred());
        Application saved = applicationRepository.save(application);
        return buildFullResponse(saved);
    }

    @Override
    public void deleteApplication(Long applicationId, Long candidateId) throws Exception {
        Application application = getApplicationEntity(applicationId);
        assertCandidate(application, candidateId);
        applicationRepository.delete(application);
    }

    @Override
    public ApplicationResponse generateCoverLetter(Long applicationId, Long candidateId) throws Exception {
        Application application = getApplicationEntity(applicationId);
        assertCandidate(application, candidateId);

        JobResponse job = jobClient.getJobById(application.getJobId());
        ResumeResponse resume = resumeClient.getResumeById(application.getResumeId(), candidateId);
        UserResponse candidate = userClient.getUserById(candidateId);
        String name = aiPromptAssembler.candidateName(
                resume,
                candidate.getFullName() != null ? candidate.getFullName() : "Candidate"
        );

        try {
            AiTextResponse generated = aiClient.generateCoverLetter(
                    aiPromptAssembler.coverLetterRequest(job, resume, name)
            );
            application.setCoverLetter(generated.getContent());
            return buildFullResponse(applicationRepository.save(application));
        } catch (Exception e) {
            log.error("Cover letter generation failed for application {}", applicationId, e);
            return buildFullResponse(application);
        }
    }

    @Override
    public SkillsGapResponse analyzeSkillsGap(Long applicationId, Long userId) throws Exception {
        Application application = getApplicationEntity(applicationId);
        if (!application.getCandidateId().equals(userId) && !application.getEmployerId().equals(userId)) {
            throw new ForbiddenException("You cannot view the skills gap for this application");
        }
        JobResponse job = jobClient.getJobById(application.getJobId());
        ResumeResponse resume = resumeClient.getResumeById(application.getResumeId(), application.getCandidateId());
        return aiClient.analyzeSkillsGap(aiPromptAssembler.skillsGapRequest(job, resume));
    }

    private void applyScreening(Application application, JobResponse job, ResumeResponse resume) {
        try {
            ScreeningScoreResponse score = aiClient.scoreCandidate(
                    aiPromptAssembler.screeningRequest(job, resume)
            );
            application.setAiScore(score.getScore());
            application.setAiShortListStatus(aiPromptAssembler.shortListStatus(score.getScore()));
        } catch (Exception e) {
            log.error("AI screening failed for application {}", application.getId(), e);
            application.setAiScore(null);
            application.setAiShortListStatus(AiShortListStatus.NOT_SCREENED);
        }
    }

    @Override
    public Application getApplicationEntity(Long applicationId) throws Exception {
        return applicationRepository.findById(applicationId)
                                    .orElseThrow(() -> new NotFoundException("Application not found with ID: " + applicationId));
    }

    public ApplicationResponse buildFullResponse(Application application) {
        List<ApplicationNote> notes = applicationNoteRepository.findByApplicationId(application.getId());
        JobResponse job = jobClient.getJobById(application.getJobId());
        CompanyResponse company = companyClient.getCompanyById(application.getCompanyId());
        UserResponse candidate = userClient.getUserById(application.getCandidateId());
        return ApplicationMapper.toResponse(application, notes, job, company, candidate);
    }

    private Sort buildSort(String sortBy) {
        if ("AI_SCORE_DESC".equals(sortBy)) {
            return Sort.by(Sort.Order.desc("aiScore").with(Sort.NullHandling.NULLS_LAST));
        } else if ("AI_SCORE_ASC".equals(sortBy)) {
            return Sort.by(Sort.Order.asc("aiScore").with(Sort.NullHandling.NULLS_LAST));
        }
        return Sort.by(Sort.Direction.DESC, "appliedAt");
    }

    private void assertEmployer(Application application, Long employerId) throws Exception {
        if (!application.getEmployerId().equals(employerId)) {
            throw new ForbiddenException("You are not the employer of this application");

        }
    }

    private void assertCandidate(Application application, Long candidateId) throws Exception {
        if (!application.getCandidateId().equals(candidateId)) {
            throw new ForbiddenException("You are not the owner of this application");

        }
    }

    private void assertViewer(Application application, Long userId) throws Exception {
        if (!application.getCandidateId().equals(userId) && !application.getEmployerId().equals(userId)) {
            throw new ForbiddenException("You cannot view this application");
        }
    }
}
