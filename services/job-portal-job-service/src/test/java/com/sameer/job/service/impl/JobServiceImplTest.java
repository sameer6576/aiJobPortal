package com.sameer.job.service.impl;

import com.sameer.job.client.CompanyClient;
import com.sameer.job.client.JobAiClient;
import com.sameer.job.domain.ExperienceLevel;
import com.sameer.job.domain.JobStatus;
import com.sameer.job.domain.JobType;
import com.sameer.job.domain.WorkMode;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.modal.Job;
import com.sameer.job.modal.JobCategory;
import com.sameer.job.repository.JobRepository;
import com.sameer.job.service.JobCategoryService;
import com.sameer.job.service.JobSkillService;
import com.sameer.job.service.JobTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobCategoryService jobCategoryService;
    @Mock
    private JobSkillService jobSkillService;
    @Mock
    private JobTagService jobTagService;
    @Mock
    private CompanyClient companyClient;
    @Mock
    private JobAiClient jobAiClient;
    @Mock
    private PlatformTransactionManager transactionManager;

    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        jobService = new JobServiceImpl(
                jobRepository,
                jobCategoryService,
                jobSkillService,
                jobTagService,
                companyClient,
                jobAiClient,
                transactionManager
        );
    }

    @Test
    void getMyJobsReturnsAllStatusesForEmployerId() {
        JobCategory category = JobCategory.builder().id(1L).name("Engineering").slug("engineering").active(true).build();
        Job draft = job(11L, JobStatus.DRAFT, category);
        Job open = job(12L, JobStatus.OPEN, category);
        when(jobRepository.findByEmployerIdOrderByCreatedAtDescIdDesc(5L)).thenReturn(List.of(open, draft));
        when(companyClient.getCompanyById(3L)).thenReturn(CompanyResponse.builder().id(3L).name("Acme").build());

        List<JobResponse> mine = jobService.getMyJobs(5L);

        assertThat(mine).extracting(JobResponse::getId).containsExactly(12L, 11L);
        assertThat(mine).extracting(JobResponse::getStatus).containsExactly(JobStatus.OPEN, JobStatus.DRAFT);
        assertThat(mine).allSatisfy(job -> assertThat(job.getCompany().getId()).isEqualTo(3L));
    }

    private static Job job(Long id, JobStatus status, JobCategory category) {
        Job job = Job.builder()
                     .id(id)
                     .title(status.name())
                     .description("desc")
                     .requirements("Java")
                     .companyId(3L)
                     .employerId(5L)
                     .category(category)
                     .jobType(JobType.FULL_TIME)
                     .workMode(WorkMode.REMOTE)
                     .experienceLevel(ExperienceLevel.MID_LEVEL)
                     .status(status)
                     .active(true)
                     .opening(1)
                     .createdAt(LocalDateTime.now())
                     .build();
        return job;
    }
}
