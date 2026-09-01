package com.sameer.job.service.impl;

import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.modal.Resume;
import com.sameer.job.payload.UpdateResumeTitleRequest;
import com.sameer.job.repository.AwardRepository;
import com.sameer.job.repository.CertificationRepository;
import com.sameer.job.repository.EducationRepository;
import com.sameer.job.repository.LanguageRepository;
import com.sameer.job.repository.ProjectRepository;
import com.sameer.job.repository.ResumeRepository;
import com.sameer.job.repository.ResumeSkillRepository;
import com.sameer.job.repository.WorkExperienceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private WorkExperienceRepository workExperienceRepository;
    @Mock
    private EducationRepository educationRepository;
    @Mock
    private ResumeSkillRepository resumeSkillRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private AwardRepository awardRepository;
    @Mock
    private CertificationRepository certificationRepository;

    private ResumeServiceImpl resumeService;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeServiceImpl(
                resumeRepository,
                workExperienceRepository,
                educationRepository,
                resumeSkillRepository,
                projectRepository,
                languageRepository,
                awardRepository,
                certificationRepository
        );
    }

    @Test
    void updateTitleChangesOwnerResumeTitle() throws Exception {
        Resume resume = Resume.builder()
                              .id(7L)
                              .candidateId(42L)
                              .title("Old title")
                              .build();
        when(resumeRepository.findById(7L)).thenReturn(Optional.of(resume));
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubNestedCollectionsEmpty(7L);

        var updated = resumeService.updateTitle(
                7L,
                42L,
                UpdateResumeTitleRequest.builder().title("  Backend CV  ").build()
        );

        assertThat(resume.getTitle()).isEqualTo("Backend CV");
        assertThat(updated.getTitle()).isEqualTo("Backend CV");
        assertThat(updated.getAwards()).isEmpty();
        assertThat(updated.getCertifications()).isEmpty();
    }

    @Test
    void updateTitleRejectsNonOwner() {
        Resume resume = Resume.builder()
                              .id(7L)
                              .candidateId(42L)
                              .title("Old title")
                              .build();
        when(resumeRepository.findById(7L)).thenReturn(Optional.of(resume));

        assertThatThrownBy(() -> resumeService.updateTitle(
                7L,
                99L,
                UpdateResumeTitleRequest.builder().title("Hacked").build()
        )).isInstanceOf(ForbiddenException.class);
    }

    private void stubNestedCollectionsEmpty(Long resumeId) {
        when(workExperienceRepository.findByResume_IdOrderByDisplayOrderAsc(resumeId)).thenReturn(List.of());
        when(educationRepository.findByResume_IdOrderByDisplayOrderAsc(resumeId)).thenReturn(List.of());
        when(resumeSkillRepository.findByResume_IdOrderByDisplayOrderAsc(resumeId)).thenReturn(List.of());
        when(projectRepository.findByResume_IdOrderByDisplayOrderAsc(resumeId)).thenReturn(List.of());
        when(languageRepository.findByResume_IdOrderByDisplayOrderAsc(resumeId)).thenReturn(List.of());
        when(awardRepository.findByResume_IdOrderByDisplayOrderAsc(resumeId)).thenReturn(List.of());
        when(certificationRepository.findByResume_IdOrderByDisplayOrderAsc(resumeId)).thenReturn(List.of());
    }
}
