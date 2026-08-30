package com.sameer.job.service.impl;

import com.sameer.job.ai.AiPromptAssembler;
import com.sameer.job.client.AiClient;
import com.sameer.job.client.CompanyClient;
import com.sameer.job.client.JobClient;
import com.sameer.job.client.ResumeClient;
import com.sameer.job.client.UserClient;
import com.sameer.job.domain.AiShortListStatus;
import com.sameer.job.domain.ExperienceLevel;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.JobSkillResponse;
import com.sameer.job.dto.ResumeResponse;
import com.sameer.job.dto.ResumeSkillResponse;
import com.sameer.job.dto.ai.ScreeningScoreResponse;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.dto.response.UserResponse;
import com.sameer.job.event.ApplicationEventPublisher;
import com.sameer.job.modal.Application;
import com.sameer.job.payload.CreateApplicationRequest;
import com.sameer.job.repository.ApplicationNoteRepository;
import com.sameer.job.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ApplicationNoteRepository applicationNoteRepository;
    @Mock
    private JobClient jobClient;
    @Mock
    private ResumeClient resumeClient;
    @Mock
    private CompanyClient companyClient;
    @Mock
    private UserClient userClient;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private AiClient aiClient;

    private ApplicationServiceImpl applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationServiceImpl(
                applicationRepository,
                applicationNoteRepository,
                jobClient,
                resumeClient,
                companyClient,
                userClient,
                applicationEventPublisher,
                aiClient,
                new AiPromptAssembler()
        );
    }

    @Test
    void createApplicationSetsAutoShortlistedWhenScoreIsHigh() throws Exception {
        stubSuccessfulApply();
        ScreeningScoreResponse score = new ScreeningScoreResponse();
        score.setScore(85);
        when(aiClient.scoreCandidate(any())).thenReturn(score);

        var response = applicationService.createApplication(5L, applyRequest());

        assertThat(response.getAiScore()).isEqualTo(85);
        assertThat(response.getAiShortListStatus()).isEqualTo(AiShortListStatus.AUTO_SHORTLISTED);
        verify(aiClient).scoreCandidate(any());
    }

    @Test
    void createApplicationLeavesNotScreenedWhenAiFails() throws Exception {
        stubSuccessfulApply();
        when(aiClient.scoreCandidate(any())).thenThrow(new RuntimeException("Gemini timeout"));

        var response = applicationService.createApplication(5L, applyRequest());

        assertThat(response.getAiScore()).isNull();
        assertThat(response.getAiShortListStatus()).isEqualTo(AiShortListStatus.NOT_SCREENED);
    }

    @Test
    void duplicateApplyNeverCallsAi() {
        when(applicationRepository.existsByCandidateIdAndJobId(5L, 9L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.createApplication(5L, applyRequest()))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("already applied");
        verify(aiClient, never()).scoreCandidate(any());
        verify(jobClient, never()).getJobById(anyLong());
    }

    @Test
    void createApplicationRejectsDraftJobs() {
        when(applicationRepository.existsByCandidateIdAndJobId(5L, 9L)).thenReturn(false);
        JobResponse draft = job();
        draft.setStatus(com.sameer.job.domain.JobStatus.DRAFT);
        when(jobClient.getJobById(9L)).thenReturn(draft);

        assertThatThrownBy(() -> applicationService.createApplication(5L, applyRequest()))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("not open");
        verify(aiClient, never()).scoreCandidate(any());
    }

    @Test
    void getApplicationByIdRejectsUnrelatedUser() {
        Application application = Application.builder()
                                               .id(1L)
                                               .candidateId(5L)
                                               .employerId(2L)
                                               .jobId(9L)
                                               .companyId(10L)
                                               .resumeId(3L)
                                               .build();
        when(applicationRepository.findById(1L)).thenReturn(java.util.Optional.of(application));

        assertThatThrownBy(() -> applicationService.getApplicationById(1L, 99L))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("cannot view");
    }

    private void stubSuccessfulApply() throws Exception {
        when(applicationRepository.existsByCandidateIdAndJobId(5L, 9L)).thenReturn(false);
        when(jobClient.getJobById(9L)).thenReturn(job());
        when(resumeClient.getResumeById(eq(3L), eq(5L))).thenReturn(resume());
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application application = invocation.getArgument(0);
            if (application.getId() == null) {
                application.setId(1L);
            }
            return application;
        });
        when(applicationNoteRepository.findByApplicationId(1L)).thenReturn(List.of());
        when(companyClient.getCompanyById(10L)).thenReturn(CompanyResponse.builder().id(10L).name("Acme").build());
        when(userClient.getUserById(5L)).thenReturn(UserResponse.builder().id(5L).fullName("Sam").email("sam@example.com").build());
    }

    private CreateApplicationRequest applyRequest() {
        return CreateApplicationRequest.builder().jobId(9L).resumeId(3L).build();
    }

    private JobResponse job() {
        return JobResponse.builder()
                           .id(9L)
                           .title("Java developer")
                           .employerId(2L)
                           .status(com.sameer.job.domain.JobStatus.OPEN)
                           .experienceLevel(ExperienceLevel.MID_LEVEL)
                           .responsibilities("Build APIs")
                           .company(CompanyResponse.builder().id(10L).name("Acme").build())
                           .skills(Set.of(JobSkillResponse.builder().name("Java").build()))
                           .build();
    }

    private ResumeResponse resume() {
        return ResumeResponse.builder()
                              .id(3L)
                              .summary("Backend engineer")
                              .skills(List.of(ResumeSkillResponse.builder().skillName("Java").build()))
                              .workExperiences(List.of())
                              .build();
    }
}
