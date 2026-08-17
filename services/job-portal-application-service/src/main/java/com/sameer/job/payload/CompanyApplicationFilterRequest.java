package com.sameer.job.payload;

import com.sameer.job.domain.AiShortListStatus;
import com.sameer.job.domain.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyApplicationFilterRequest {
    private Long jobId;
    private ApplicationStatus status;
    private Boolean isStarred;
    private AiShortListStatus aiShortListStatus;
    private Integer minAiScore;
    private String sortBy;
}
