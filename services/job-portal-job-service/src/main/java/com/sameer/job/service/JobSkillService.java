package com.sameer.job.service;

import com.sameer.job.dto.JobSkillResponse;
import com.sameer.job.modal.JobSkill;
import com.sameer.job.payload.JobSkillRequest;

import java.util.List;
import java.util.Set;

public interface JobSkillService {

    JobSkillResponse createSkill(JobSkillRequest req) throws Exception;

    List<JobSkillResponse> getAllSkills();

    JobSkillResponse getSkillById(Long id) throws Exception;

    JobSkillResponse updateSkills(Long id, JobSkillRequest req) throws Exception;

    void deleteSkill(Long id) throws Exception;

    Set<JobSkill> getSkillByIds(Set<Long> ids);

}
