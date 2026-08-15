package com.sameer.job.service;

import com.sameer.job.dto.ResumeSkillResponse;
import com.sameer.job.modal.ResumeSkill;
import com.sameer.job.payload.AddResumeSkillRequest;

import java.util.List;

public interface ResumeSkillService {
    ResumeSkillResponse addSkill(Long resumeId, Long candidateId, AddResumeSkillRequest req) throws Exception;

    List<ResumeSkillResponse> getSkills(Long resumeId);

    ResumeSkillResponse updateSkill (Long skillId, Long resumeId, Long candidateId, AddResumeSkillRequest req) throws Exception;

    ResumeSkill getSkillEntity(Long skillId) throws Exception;

    void deleteSkill(Long skillId, Long resumeId, Long candidateId) throws Exception;
}
