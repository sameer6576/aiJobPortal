package com.sameer.job.controller;

import com.sameer.job.dto.LanguageResponse;
import com.sameer.job.payload.AddLanguageRequest;
import com.sameer.job.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes/{resumeId}/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @PostMapping
    public ResponseEntity<LanguageResponse> addLanguage(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddLanguageRequest request
    ) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(
                                     languageService.addLanguage(
                                             resumeId,
                                             candidateId,
                                             request
                                     )
                             );
    }

    @GetMapping
    public ResponseEntity<List<LanguageResponse>> getLanguages(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        return ResponseEntity.ok(
                languageService.getLanguages(resumeId, candidateId)
        );
    }

    @PutMapping("/{languageId}")
    public ResponseEntity<LanguageResponse> updateLanguage(
            @PathVariable Long resumeId,
            @PathVariable Long languageId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddLanguageRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                languageService.updateLanguage(
                        languageId,
                        resumeId,
                        candidateId,
                        request
                )
        );
    }

    @DeleteMapping("/{languageId}")
    public ResponseEntity<Void> deleteLanguage(
            @PathVariable Long resumeId,
            @PathVariable Long languageId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        languageService.deleteLanguage(
                languageId,
                resumeId,
                candidateId
        );

        return ResponseEntity.noContent().build();
    }
}