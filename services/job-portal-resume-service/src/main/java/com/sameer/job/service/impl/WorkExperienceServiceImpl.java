package com.sameer.job.service.impl;

import com.sameer.job.dto.WorkExperienceResponse;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.WorkExperienceMapper;
import com.sameer.job.modal.Resume;
import com.sameer.job.modal.WorkExperience;
import com.sameer.job.payload.AddWorkExperience;
import com.sameer.job.repository.WorkExperienceRepository;
import com.sameer.job.service.ResumeService;
import com.sameer.job.service.WorkExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkExperienceServiceImpl implements WorkExperienceService {

    private final ResumeService resumeService;
    private final WorkExperienceRepository workExperienceRepository;

    @Override
    public WorkExperienceResponse addWorkExperience(Long resumeId, Long candidateId, AddWorkExperience req) throws Exception {
        Resume resume = resumeService.getResumeEntity(resumeId);
        assertOwner(resume, candidateId);

        WorkExperience workExperience = WorkExperience.builder()
                                                      .resume(resume)
                                                      .companyName(req.getCompanyName())
                                                      .companyLogoUrl(req.getCompanyLogoUrl())
                                                      .description(req.getDescription())
                                                      .jobTitle(req.getJobTitle())
                                                      .employmentType(req.getEmploymentType())
                                                      .location(req.getLocation())
                                                      .startDate(req.getStartDate())
                                                      .endDate(req.getEndDate())
                                                      .isCurrentJob(req.getIsCurrentJob())
                                                      .technologies(req.getTechnologies() != null ? req.getTechnologies() : List.of())
                                                      .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0)
                                                      .build();
        WorkExperience saved = workExperienceRepository.save(workExperience);
        return WorkExperienceMapper.toWorkExperienceResponse(saved);
    }

    @Override
    public List<WorkExperienceResponse> getWorkExperiences(Long resumeId, Long candidateId) throws Exception {
        Resume resume = resumeService.getResumeEntity(resumeId);
        assertOwner(resume, candidateId);
        return workExperienceRepository.findByResume_IdOrderByDisplayOrderAsc(resumeId).stream()
                                       .map(WorkExperienceMapper::toWorkExperienceResponse).toList();
    }

    @Override
    public WorkExperienceResponse updateWorkExperience(Long resumeId, Long workExperienceId, Long candidateId, AddWorkExperience req) throws Exception {
        WorkExperience exp = getWorkExperienceEntity(workExperienceId);

        if (!exp.getResume().getId().equals(resumeId)) {
            throw new ForbiddenException("Work experience does not belong to this resume");
        }

        assertOwner(exp.getResume(), candidateId);

        exp.setCompanyName(req.getCompanyName());
        exp.setCompanyLogoUrl(req.getCompanyLogoUrl());
        exp.setJobTitle(req.getJobTitle());
        exp.setEmploymentType(req.getEmploymentType());
        exp.setLocation(req.getLocation());
        exp.setStartDate(req.getStartDate());
        exp.setEndDate(req.getEndDate());
        exp.setIsCurrentJob(Boolean.TRUE.equals(req.getIsCurrentJob()));
        exp.setDescription(req.getDescription());
        if(req.getTechnologies() !=null) exp.setTechnologies(req.getTechnologies());
        if(req.getDisplayOrder() !=null) exp.setDisplayOrder(req.getDisplayOrder());

        WorkExperience saved = workExperienceRepository.save(exp);
        return WorkExperienceMapper.toWorkExperienceResponse(saved);
    }

    @Override
    public void deleteWorkExperience(Long resumeId, Long workExperienceId, Long candidateId) throws Exception {
        WorkExperience exp = getWorkExperienceEntity(workExperienceId);

        if (!exp.getResume().getId().equals(resumeId)) {
            throw new ForbiddenException("Work experience does not belong to this resume");
        }

        assertOwner(exp.getResume(), candidateId);
        workExperienceRepository.delete(exp);

    }

    @Override
    public WorkExperience getWorkExperienceEntity(Long workExperienceId) throws Exception {
        return workExperienceRepository.findById(workExperienceId)
                                       .orElseThrow(() -> new NotFoundException("Work Experience not found with ID: " + workExperienceId));
    }

    private void assertOwner(Resume resume, Long candidateId) throws Exception {
        if (!resume.getCandidateId().equals(candidateId)) {
            throw new ForbiddenException("This resume does not belong to this candidate");
        }
    }


}
