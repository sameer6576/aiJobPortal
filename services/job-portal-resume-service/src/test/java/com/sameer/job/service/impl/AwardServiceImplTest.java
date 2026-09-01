package com.sameer.job.service.impl;

import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.modal.Award;
import com.sameer.job.modal.Resume;
import com.sameer.job.payload.AddAwardRequest;
import com.sameer.job.repository.AwardRepository;
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
class AwardServiceImplTest {

    @Mock
    private AwardRepository awardRepository;
    @Mock
    private ResumeService resumeService;

    private AwardServiceImpl awardService;

    @BeforeEach
    void setUp() {
        awardService = new AwardServiceImpl(awardRepository, resumeService);
    }

    @Test
    void addAwardPersistsOwnedAward() throws Exception {
        Resume resume = Resume.builder().id(1L).candidateId(10L).title("CV").build();
        when(resumeService.getResumeEntity(1L)).thenReturn(resume);
        when(awardRepository.save(any(Award.class))).thenAnswer(invocation -> {
            Award award = invocation.getArgument(0);
            award.setId(5L);
            return award;
        });

        var response = awardService.addAward(
                1L,
                10L,
                AddAwardRequest.builder()
                               .title("Dean list")
                               .issuedBy("University")
                               .awardDate(LocalDate.of(2024, 5, 1))
                               .description("Top 5%")
                               .displayOrder(2)
                               .build()
        );

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getTitle()).isEqualTo("Dean list");
        assertThat(response.getIssuedBy()).isEqualTo("University");
        assertThat(response.getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void updateAwardRejectsItemFromAnotherResume() throws Exception {
        Resume otherResume = Resume.builder().id(2L).candidateId(10L).title("Other").build();
        Award award = Award.builder().id(8L).resume(otherResume).title("Old").build();
        when(awardRepository.findById(8L)).thenReturn(Optional.of(award));

        assertThatThrownBy(() -> awardService.updateAward(
                8L,
                1L,
                10L,
                AddAwardRequest.builder().title("New").build()
        )).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteAwardRemovesOwnedItem() throws Exception {
        Resume resume = Resume.builder().id(1L).candidateId(10L).title("CV").build();
        Award award = Award.builder().id(8L).resume(resume).title("Old").build();
        when(awardRepository.findById(8L)).thenReturn(Optional.of(award));

        awardService.deleteAward(8L, 1L, 10L);

        verify(awardRepository).delete(award);
    }
}
