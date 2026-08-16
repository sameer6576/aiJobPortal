package com.sameer.job.controller;

import com.sameer.job.dto.PersonalInfoResponse;
import com.sameer.job.dto.ResumeResponse;
import com.sameer.job.dto.response.ApiResponse;
import com.sameer.job.payload.CreateResumeRequest;
import com.sameer.job.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody CreateResumeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(resumeService.createResume(candidateId, request));
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> getResumeById(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        return ResponseEntity.ok(
                resumeService.getResumeById(resumeId, candidateId)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<ResumeResponse>> getMyResumes(
            @RequestHeader("X-User-Id") Long candidateId
    ) {

        return ResponseEntity.ok(
                resumeService.getMyResumes(candidateId)
        );
    }

    @PutMapping("/{resumeId}/personal-info")
    public ResponseEntity<ResumeResponse> updatePersonalInfo(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody PersonalInfoResponse request
    ) throws Exception {

        return ResponseEntity.ok(
                resumeService.updatePersonalInfo(
                        resumeId,
                        candidateId,
                        request
                )
        );
    }

    @PutMapping("/{resumeId}/summary")
    public ResponseEntity<ResumeResponse> updateSummary(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId,
            @RequestBody String summary
    ) throws Exception {

        return ResponseEntity.ok(
                resumeService.updateSummary(
                        resumeId,
                        candidateId,
                        summary
                )
        );
    }

    @PutMapping("/{resumeId}/default")
    public ResponseEntity<ResumeResponse> setDefaultResume(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        return ResponseEntity.ok(
                resumeService.setDefaultResume(
                        resumeId,
                        candidateId
                )
        );
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<ApiResponse> deleteResume(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        resumeService.deleteResume(
                resumeId,
                candidateId
        );

        return ResponseEntity.ok(new ApiResponse("Resume deleted succesfully", true));
    }
}