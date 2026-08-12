package com.sameer.job.mapper;

import com.sameer.job.dto.JobSkillResponse;
import com.sameer.job.modal.JobSkill;

public class JobSkillMapper {

    public static JobSkillResponse toJobSkillResponse(JobSkill jobSkill){
        return JobSkillResponse
                .builder()
                .id(jobSkill.getId())
                .name(jobSkill.getName())
                .slug(jobSkill.getSlug())
                .category(jobSkill.getCategory())
                .active(jobSkill.getActive())
                .build();
    }
}
