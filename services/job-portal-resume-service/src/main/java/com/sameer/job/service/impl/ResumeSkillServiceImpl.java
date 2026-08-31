package com.sameer.job.service.impl;

import com.sameer.job.dto.ResumeSkillResponse;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.ResumeMapper;
import com.sameer.job.modal.Resume;
import com.sameer.job.modal.ResumeSkill;
import com.sameer.job.payload.AddResumeSkillRequest;
import com.sameer.job.repository.ResumeSkillRepository;
import com.sameer.job.service.ResumeService;
import com.sameer.job.service.ResumeSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ResumeSkillServiceImpl implements ResumeSkillService {

    private final ResumeSkillRepository resumeSkillRepository;
    private final ResumeService resumeService;

    @Override
    public ResumeSkillResponse addSkill(Long resumeId, Long candidateId, AddResumeSkillRequest req) throws Exception {

        Resume resume = resumeService.getResumeEntity(resumeId);
        assertOwner(resume, candidateId);
        ResumeSkill resumeSkill = ResumeSkill.builder()
                                             .resume(resume).skillName(req.getSkillName())
                                             .proficiencyLevel(req.getProficiencyLevel())
                                             .yearsOfExperience(req.getYearsOfExperience())
                                             .displayOrder(req.getDisplayOrder())
                                             .build();
        ResumeSkill saved = resumeSkillRepository.save(resumeSkill);
        return ResumeMapper.toSkillResponse(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeSkillResponse> getSkills(Long resumeId, Long candidateId) throws Exception {
        Resume resume = resumeService.getResumeEntity(resumeId);
        assertOwner(resume, candidateId);
        return resumeSkillRepository.findByResume_IdOrderByDisplayOrderAsc(resumeId).stream()
                                    .map(ResumeMapper::toSkillResponse).toList();
    }

    @Override
    public ResumeSkillResponse updateSkill(Long skillId, Long resumeId, Long candidateId, AddResumeSkillRequest req) throws Exception {
        ResumeSkill resumeSkill = getSkillEntity(skillId);
        assertOwner(resumeSkill.getResume(), candidateId);
        resumeSkill.setSkillName(req.getSkillName());
        resumeSkill.setProficiencyLevel(req.getProficiencyLevel());
        resumeSkill.setYearsOfExperience(req.getYearsOfExperience());
        if(req.getDisplayOrder() != null) resumeSkill.setDisplayOrder(req.getDisplayOrder());

        return ResumeMapper.toSkillResponse(resumeSkillRepository.save(resumeSkill));
    }

    @Override
    public ResumeSkill getSkillEntity(Long skillId) throws Exception {
        return resumeSkillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found with ID: "+ skillId));
    }

    @Override
    public void deleteSkill(Long skillId, Long resumeId, Long candidateId) throws Exception {
        ResumeSkill resumeSkill = getSkillEntity(skillId);
        assertOwner(resumeSkill.getResume(), candidateId);
        resumeSkillRepository.delete(resumeSkill);
    }

    private void assertOwner(Resume resume, Long candidateId) throws Exception {
        if (!resume.getCandidateId().equals(candidateId)) {
            throw new ForbiddenException("This resume does not belong to this candidate");
        }
    }
}
