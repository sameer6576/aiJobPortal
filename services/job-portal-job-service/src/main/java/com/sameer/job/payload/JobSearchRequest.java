package com.sameer.job.payload;

import com.sameer.job.domain.ExperienceLevel;
import com.sameer.job.domain.JobStatus;
import com.sameer.job.domain.JobType;
import com.sameer.job.domain.WorkMode;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchRequest {

    private String keyword;

    private Long id;

    private List<Long> skillIds;

    private List<Long> tagIds;

    private Long companyId;

    private Long categoryId;

    private String location;

    private BigDecimal minSalary;

    private BigDecimal maxSalary;

    private JobType jobType;

    private WorkMode workMode;

    private ExperienceLevel experienceLevel;

    private JobStatus status;

    private Integer minOpenings;
    private Integer maxOpenings;

}
