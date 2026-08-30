package com.sameer.job.controller;

import com.sameer.job.dto.WorkExperienceResponse;
import com.sameer.job.payload.AddWorkExperience;
import com.sameer.job.service.WorkExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes/{resumeId}/work-experiences")
@RequiredArgsConstructor
public class WorkExperienceController {

    private final WorkExperienceService workExperienceService;

    @PostMapping
    public ResponseEntity<WorkExperienceResponse> addWorkExperience(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddWorkExperience request
    ) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(
                                     workExperienceService.addWorkExperience(
                                             resumeId,
                                             candidateId,
                                             request
                                     )
                             );
    }

    @GetMapping
    public ResponseEntity<List<WorkExperienceResponse>> getWorkExperiences(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        return ResponseEntity.ok(
                workExperienceService.getWorkExperiences(resumeId, candidateId)
        );
    }

    @PutMapping("/{workExperienceId}")
    public ResponseEntity<WorkExperienceResponse> updateWorkExperience(
            @PathVariable Long resumeId,
            @PathVariable Long workExperienceId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddWorkExperience request
    ) throws Exception {

        return ResponseEntity.ok(
                workExperienceService.updateWorkExperience(
                        resumeId,
                        workExperienceId,
                        candidateId,
                        request
                )
        );
    }

    @DeleteMapping("/{workExperienceId}")
    public ResponseEntity<Void> deleteWorkExperience(
            @PathVariable Long resumeId,
            @PathVariable Long workExperienceId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        workExperienceService.deleteWorkExperience(
                resumeId,
                workExperienceId,
                candidateId
        );

        return ResponseEntity.noContent().build();
    }
}