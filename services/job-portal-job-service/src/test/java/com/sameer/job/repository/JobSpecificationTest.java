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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
