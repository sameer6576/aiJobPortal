package com.sameer.job.mapper;

import com.sameer.job.dto.JobTagResponse;
import com.sameer.job.modal.JobTag;

public class JobTagMapper {

    public static JobTagResponse toJobTagResponse(JobTag jobTag) {
        return JobTagResponse
                .builder()
                .id(jobTag.getId())
                .name(jobTag.getName())
                .slug(jobTag.getSlug())
                .build();
    }
}
