package com.sameer.job.repository;

import com.sameer.job.domain.ExperienceLevel;
import com.sameer.job.domain.JobStatus;
import com.sameer.job.domain.JobType;
import com.sameer.job.domain.WorkMode;
import com.sameer.job.modal.Job;
import com.sameer.job.modal.JobCategory;
import com.sameer.job.payload.JobSearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.openfeign.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:jobs;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class JobSpecificationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobCategoryRepository jobCategoryRepository;

    @Test
    void filtersByCategoryAndOpening() {
        JobCategory engineering = jobCategoryRepository.save(
                JobCategory.builder().name("Engineering").slug("engineering").active(true).build()
        );
        JobCategory design = jobCategoryRepository.save(
                JobCategory.builder().name("Design").slug("design").active(true).build()
        );

        jobRepository.save(openJob("Java engineer", engineering, 3));
        jobRepository.save(openJob("Designer", design, 1));
        jobRepository.save(openJob("Staff engineer", engineering, 1));

        JobSearchRequest request = new JobSearchRequest();
        request.setCategoryId(engineering.getId());
        request.setMinOpenings(2);

        List<Job> matches = jobRepository.findAll(JobSpecification.build(request));

        assertThat(matches).extracting(Job::getTitle).containsExactly("Java engineer");
    }

    @Test
    void findByEmployerIdReturnsEveryStatusNewestFirst() {
        JobCategory engineering = jobCategoryRepository.save(
                JobCategory.builder().name("Engineering").slug("engineering-my").active(true).build()
        );

        Job olderDraft = jobRepository.save(job("Older draft", engineering, 2L, JobStatus.DRAFT));
        Job otherEmployer = jobRepository.save(job("Other employer", engineering, 9L, JobStatus.OPEN));
        Job newerClosed = jobRepository.save(job("Newer closed", engineering, 2L, JobStatus.CLOSED));

        List<Job> mine = jobRepository.findByEmployerIdOrderByCreatedAtDescIdDesc(2L);

        assertThat(mine).extracting(Job::getTitle).containsExactly("Newer closed", "Older draft");
        assertThat(mine).extracting(Job::getStatus).containsExactly(JobStatus.CLOSED, JobStatus.DRAFT);
        assertThat(mine).extracting(Job::getId).doesNotContain(otherEmployer.getId());
        assertThat(mine.get(0).getId()).isGreaterThan(olderDraft.getId());
        assertThat(newerClosed.getId()).isGreaterThan(olderDraft.getId());
    }

    private Job job(String title, JobCategory category, long employerId, JobStatus status) {
        return Job.builder()
                   .title(title)
                   .description(title + " description")
                   .requirements("Java")
                   .companyId(1L)
                   .employerId(employerId)
                   .category(category)
                   .jobType(JobType.FULL_TIME)
                   .workMode(WorkMode.REMOTE)
                   .experienceLevel(ExperienceLevel.MID_LEVEL)
                   .status(status)
                   .active(status != JobStatus.CLOSED)
                   .opening(1)
                   .build();
    }

    private Job openJob(String title, JobCategory category, int opening) {
        return Job.builder()
                   .title(title)
                   .description(title + " description")
                   .requirements("Java")
                   .companyId(1L)
                   .employerId(2L)
                   .category(category)
                   .jobType(JobType.FULL_TIME)
                   .workMode(WorkMode.REMOTE)
                   .experienceLevel(ExperienceLevel.MID_LEVEL)
                   .status(JobStatus.OPEN)
                   .active(true)
                   .opening(opening)
                   .build();
    }
}
