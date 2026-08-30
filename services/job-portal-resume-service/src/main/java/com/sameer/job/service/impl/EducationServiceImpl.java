package com.sameer.job.service.impl;

import com.sameer.job.dto.EducationResponse;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.ResumeMapper;
import com.sameer.job.modal.Education;
import com.sameer.job.modal.Resume;
import com.sameer.job.payload.AddEducationRequest;
import com.sameer.job.repository.EducationRepository;
import com.sameer.job.service.EducationService;
import com.sameer.job.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final ResumeService resumeService;

    @Override
    @Transactional
    public EducationResponse addEducation(
            Long resumeId,
            Long candidateId,
            AddEducationRequest request
    ) throws Exception {

        Resume resume = resumeService.getResumeEntity(resumeId);

        assertOwner(resume, candidateId);

        Education education = Education.builder()
                                       .resume(resume)
                                       .institutionName(request.getInstitutionName())
                                       .degree(request.getDegree())
                                       .fieldOfStudy(request.getFieldOfStudy())
                                       .grade(request.getGrade())
                                       .description(request.getDescription())
                                       .startDate(request.getStartDate())
                                       .endDate(request.getEndDate())
                                       .isCurrentlyStudying(Boolean.TRUE.equals(request.getIsCurrentlyStudying()))
                                       .displayOrder(request.getDisplayOrder() != null
                                               ? request.getDisplayOrder()
                                               : 0)
                                       .build();

        Education saved = educationRepository.save(education);

        return ResumeMapper.toEducationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationResponse> getEducations(Long resumeId, Long candidateId) throws Exception {
        resumeService.requireOwner(resumeId, candidateId);
        return educationRepository
                .findByResume_IdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .map(ResumeMapper::toEducationResponse)
                .toList();
    }

    @Override
    @Transactional
    public EducationResponse updateEducation(
            Long educationId,
            Long resumeId,
            Long candidateId,
            AddEducationRequest request
    ) throws Exception {

        Education education = getOwnedEducation(
                educationId,
                resumeId,
                candidateId
        );

        education.setInstitutionName(request.getInstitutionName());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setGrade(request.getGrade());
        education.setDescription(request.getDescription());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setIsCurrentlyStudying(
                Boolean.TRUE.equals(request.getIsCurrentlyStudying())
        );

        if (request.getDisplayOrder() != null) {
            education.setDisplayOrder(request.getDisplayOrder());
        }

        Education saved = educationRepository.save(education);

        return ResumeMapper.toEducationResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEducation(
            Long educationId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Education education = getOwnedEducation(
                educationId,
                resumeId,
                candidateId
        );

        educationRepository.delete(education);
    }

    private Education getOwnedEducation(
            Long educationId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Education education = educationRepository.findById(educationId)
                                                 .orElseThrow(() ->
                                                         new NotFoundException("Education not found with ID: " + educationId));

        if (!education.getResume().getId().equals(resumeId)) {
            throw new ForbiddenException("Education does not belong to this resume");
        }

        assertOwner(education.getResume(), candidateId);

        return education;
    }

    private void assertOwner(
            Resume resume,
            Long candidateId
    ) throws Exception {

        if (!resume.getCandidateId().equals(candidateId)) {
            throw new NotFoundException("Resume not found");
        }
    }
}