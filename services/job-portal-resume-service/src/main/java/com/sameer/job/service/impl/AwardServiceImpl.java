package com.sameer.job.service.impl;

import com.sameer.job.dto.AwardResponse;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.ResumeMapper;
import com.sameer.job.modal.Award;
import com.sameer.job.modal.Resume;
import com.sameer.job.payload.AddAwardRequest;
import com.sameer.job.repository.AwardRepository;
import com.sameer.job.service.AwardService;
import com.sameer.job.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AwardServiceImpl implements AwardService {

    private final AwardRepository awardRepository;
    private final ResumeService resumeService;

    @Override
    @Transactional
    public AwardResponse addAward(
            Long resumeId,
            Long candidateId,
            AddAwardRequest request
    ) throws Exception {

        Resume resume = resumeService.getResumeEntity(resumeId);
        assertOwner(resume, candidateId);

        Award award = Award.builder()
                           .resume(resume)
                           .title(request.getTitle())
                           .issuedBy(request.getIssuedBy())
                           .awardDate(request.getAwardDate())
                           .description(request.getDescription())
                           .displayOrder(request.getDisplayOrder() != null
                                   ? request.getDisplayOrder()
                                   : 0)
                           .build();

        Award saved = awardRepository.save(award);

        return ResumeMapper.toAwardResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AwardResponse> getAwards(Long resumeId, Long candidateId) throws Exception {
        resumeService.requireOwner(resumeId, candidateId);
        return awardRepository
                .findByResume_IdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .map(ResumeMapper::toAwardResponse)
                .toList();
    }

    @Override
    @Transactional
    public AwardResponse updateAward(
            Long awardId,
            Long resumeId,
            Long candidateId,
            AddAwardRequest request
    ) throws Exception {

        Award award = getOwnedAward(awardId, resumeId, candidateId);

        award.setTitle(request.getTitle());
        award.setIssuedBy(request.getIssuedBy());
        award.setAwardDate(request.getAwardDate());
        award.setDescription(request.getDescription());

        if (request.getDisplayOrder() != null) {
            award.setDisplayOrder(request.getDisplayOrder());
        }

        Award saved = awardRepository.save(award);

        return ResumeMapper.toAwardResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAward(
            Long awardId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Award award = getOwnedAward(awardId, resumeId, candidateId);
        awardRepository.delete(award);
    }

    private Award getOwnedAward(
            Long awardId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Award award = awardRepository.findById(awardId)
                                     .orElseThrow(() ->
                                             new NotFoundException("Award not found with ID: " + awardId));

        if (!award.getResume().getId().equals(resumeId)) {
            throw new ForbiddenException("Award does not belong to this resume");
        }

        assertOwner(award.getResume(), candidateId);

        return award;
    }

    private void assertOwner(Resume resume, Long candidateId) {
        if (!resume.getCandidateId().equals(candidateId)) {
            throw new ForbiddenException("This resume does not belong to this candidate");
        }
    }
}
