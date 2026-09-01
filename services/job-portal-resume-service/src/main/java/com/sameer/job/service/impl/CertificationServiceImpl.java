package com.sameer.job.service.impl;

import com.sameer.job.dto.CertificationResponse;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.ResumeMapper;
import com.sameer.job.modal.Certification;
import com.sameer.job.modal.Resume;
import com.sameer.job.payload.AddCertificationRequest;
import com.sameer.job.repository.CertificationRepository;
import com.sameer.job.service.CertificationService;
import com.sameer.job.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {

    private final CertificationRepository certificationRepository;
    private final ResumeService resumeService;

    @Override
    @Transactional
    public CertificationResponse addCertification(
            Long resumeId,
            Long candidateId,
            AddCertificationRequest request
    ) throws Exception {

        Resume resume = resumeService.getResumeEntity(resumeId);
        assertOwner(resume, candidateId);

        Certification certification = Certification.builder()
                                                   .resume(resume)
                                                   .name(request.getName())
                                                   .issuingOrganization(request.getIssuingOrganization())
                                                   .issueDate(request.getIssueDate())
                                                   .expiryDate(request.getExpiryDate())
                                                   .credentialId(request.getCredentialId())
                                                   .credentialUrl(request.getCredentialUrl())
                                                   .displayOrder(request.getDisplayOrder() != null
                                                           ? request.getDisplayOrder()
                                                           : 0)
                                                   .build();

        Certification saved = certificationRepository.save(certification);

        return ResumeMapper.toCertificationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> getCertifications(Long resumeId, Long candidateId) throws Exception {
        resumeService.requireOwner(resumeId, candidateId);
        return certificationRepository
                .findByResume_IdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .map(ResumeMapper::toCertificationResponse)
                .toList();
    }

    @Override
    @Transactional
    public CertificationResponse updateCertification(
            Long certificationId,
            Long resumeId,
            Long candidateId,
            AddCertificationRequest request
    ) throws Exception {

        Certification certification = getOwnedCertification(certificationId, resumeId, candidateId);

        certification.setName(request.getName());
        certification.setIssuingOrganization(request.getIssuingOrganization());
        certification.setIssueDate(request.getIssueDate());
        certification.setExpiryDate(request.getExpiryDate());
        certification.setCredentialId(request.getCredentialId());
        certification.setCredentialUrl(request.getCredentialUrl());

        if (request.getDisplayOrder() != null) {
            certification.setDisplayOrder(request.getDisplayOrder());
        }

        Certification saved = certificationRepository.save(certification);

        return ResumeMapper.toCertificationResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCertification(
            Long certificationId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Certification certification = getOwnedCertification(certificationId, resumeId, candidateId);
        certificationRepository.delete(certification);
    }

    private Certification getOwnedCertification(
            Long certificationId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Certification certification = certificationRepository.findById(certificationId)
                                                             .orElseThrow(() ->
                                                                     new NotFoundException(
                                                                             "Certification not found with ID: " + certificationId
                                                                     ));

        if (!certification.getResume().getId().equals(resumeId)) {
            throw new ForbiddenException("Certification does not belong to this resume");
        }

        assertOwner(certification.getResume(), candidateId);

        return certification;
    }

    private void assertOwner(Resume resume, Long candidateId) {
        if (!resume.getCandidateId().equals(candidateId)) {
            throw new ForbiddenException("This resume does not belong to this candidate");
        }
    }
}
