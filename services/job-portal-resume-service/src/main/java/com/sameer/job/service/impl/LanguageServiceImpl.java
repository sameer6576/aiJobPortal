package com.sameer.job.service.impl;

import com.sameer.job.dto.LanguageResponse;
import com.sameer.job.mapper.ResumeMapper;
import com.sameer.job.modal.Language;
import com.sameer.job.modal.Resume;
import com.sameer.job.payload.AddLanguageRequest;
import com.sameer.job.repository.LanguageRepository;
import com.sameer.job.service.LanguageService;
import com.sameer.job.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;
    private final ResumeService resumeService;

    @Override
    @Transactional
    public LanguageResponse addLanguage(
            Long resumeId,
            Long candidateId,
            AddLanguageRequest req
    ) throws Exception {

        Resume resume = resumeService.getResumeEntity(resumeId);
        assertOwner(resume, candidateId);

        Language language = Language.builder()
                                    .resume(resume)
                                    .languageName(req.getLanguageName())
                                    .proficiency(req.getProficiency())
                                    .displayOrder(
                                            req.getDisplayOrder() != null
                                                    ? req.getDisplayOrder()
                                                    : 0
                                    )
                                    .build();

        Language saved = languageRepository.save(language);

        return ResumeMapper.toLanguageResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanguageResponse> getLanguages(Long resumeId, Long candidateId) throws Exception {
        resumeService.requireOwner(resumeId, candidateId);
        return languageRepository
                .findByResume_IdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .map(ResumeMapper::toLanguageResponse)
                .toList();
    }

    @Override
    @Transactional
    public LanguageResponse updateLanguage(
            Long languageId,
            Long resumeId,
            Long candidateId,
            AddLanguageRequest req
    ) throws Exception {

        Language language = getOwnedLanguage(
                languageId,
                resumeId,
                candidateId
        );

        language.setLanguageName(req.getLanguageName());
        language.setProficiency(req.getProficiency());

        if (req.getDisplayOrder() != null) {
            language.setDisplayOrder(req.getDisplayOrder());
        }

        Language saved = languageRepository.save(language);

        return ResumeMapper.toLanguageResponse(saved);
    }

    @Override
    @Transactional
    public void deleteLanguage(
            Long languageId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Language language = getOwnedLanguage(
                languageId,
                resumeId,
                candidateId
        );

        languageRepository.delete(language);
    }

    private Language getOwnedLanguage(
            Long languageId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Language language = languageRepository.findById(languageId)
                                              .orElseThrow(() ->
                                                      new Exception(
                                                              "Language not found with ID: " + languageId
                                                      )
                                              );

        if (!language.getResume().getId().equals(resumeId)) {
            throw new Exception(
                    "Language does not belong to this resume"
            );
        }

        assertOwner(language.getResume(), candidateId);

        return language;
    }

    private void assertOwner(
            Resume resume,
            Long candidateId
    ) throws Exception {

        if (!resume.getCandidateId().equals(candidateId)) {
            throw new Exception("Resume not found");
        }
    }
}