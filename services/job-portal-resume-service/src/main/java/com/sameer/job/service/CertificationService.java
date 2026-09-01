package com.sameer.job.service;

import com.sameer.job.dto.CertificationResponse;
import com.sameer.job.payload.AddCertificationRequest;

import java.util.List;

public interface CertificationService {
    CertificationResponse addCertification(Long resumeId, Long candidateId, AddCertificationRequest request) throws Exception;

    List<CertificationResponse> getCertifications(Long resumeId, Long candidateId) throws Exception;

    CertificationResponse updateCertification(Long certificationId, Long resumeId, Long candidateId, AddCertificationRequest request) throws Exception;

    void deleteCertification(Long certificationId, Long resumeId, Long candidateId) throws Exception;
}
