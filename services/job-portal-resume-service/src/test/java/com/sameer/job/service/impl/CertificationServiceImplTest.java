package com.sameer.job.service.impl;

import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.modal.Certification;
import com.sameer.job.modal.Resume;
import com.sameer.job.payload.AddCertificationRequest;
import com.sameer.job.repository.CertificationRepository;
import com.sameer.job.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificationServiceImplTest {

    @Mock
    private CertificationRepository certificationRepository;
    @Mock
    private ResumeService resumeService;

    private CertificationServiceImpl certificationService;

    @BeforeEach
    void setUp() {
        certificationService = new CertificationServiceImpl(certificationRepository, resumeService);
    }

    @Test
    void addCertificationPersistsOwnedCertification() throws Exception {
        Resume resume = Resume.builder().id(1L).candidateId(10L).title("CV").build();
        when(resumeService.getResumeEntity(1L)).thenReturn(resume);
        when(certificationRepository.save(any(Certification.class))).thenAnswer(invocation -> {
            Certification certification = invocation.getArgument(0);
            certification.setId(9L);
            return certification;
        });

        var response = certificationService.addCertification(
                1L,
                10L,
                AddCertificationRequest.builder()
                                       .name("AWS SAA")
                                       .issuingOrganization("Amazon")
                                       .issueDate(LocalDate.of(2023, 1, 15))
                                       .expiryDate(LocalDate.of(2026, 1, 15))
                                       .credentialId("ABC-123")
                                       .credentialUrl("https://aws.amazon.com/verify/ABC-123")
                                       .displayOrder(1)
                                       .build()
        );

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getName()).isEqualTo("AWS SAA");
        assertThat(response.getIssuingOrganization()).isEqualTo("Amazon");
        assertThat(response.getCredentialId()).isEqualTo("ABC-123");
        assertThat(response.getExpiryDate()).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void deleteCertificationRejectsNonOwner() {
        Resume resume = Resume.builder().id(1L).candidateId(10L).title("CV").build();
        Certification certification = Certification.builder()
                                                   .id(9L)
                                                   .resume(resume)
                                                   .name("AWS SAA")
                                                   .build();
        when(certificationRepository.findById(9L)).thenReturn(Optional.of(certification));

        assertThatThrownBy(() -> certificationService.deleteCertification(9L, 1L, 99L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteCertificationRemovesOwnedItem() throws Exception {
        Resume resume = Resume.builder().id(1L).candidateId(10L).title("CV").build();
        Certification certification = Certification.builder()
                                                   .id(9L)
                                                   .resume(resume)
                                                   .name("AWS SAA")
                                                   .build();
        when(certificationRepository.findById(9L)).thenReturn(Optional.of(certification));

        certificationService.deleteCertification(9L, 1L, 10L);

        verify(certificationRepository).delete(certification);
    }
}
