package com.sameer.job.controller;

import com.sameer.job.dto.EducationResponse;
import com.sameer.job.dto.response.ApiResponse;
import com.sameer.job.payload.AddEducationRequest;
import com.sameer.job.service.EducationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes/{resumeId}/educations")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    @PostMapping
    public ResponseEntity<EducationResponse> addEducation(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddEducationRequest request
    ) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(
                                     educationService.addEducation(
                                             resumeId,
                                             candidateId,
                                             request
                                     )
                             );
    }

    @GetMapping
    public ResponseEntity<List<EducationResponse>> getEducations(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        return ResponseEntity.ok(
                educationService.getEducations(resumeId, candidateId)
        );
    }

    @PutMapping("/{educationId}")
    public ResponseEntity<EducationResponse> updateEducation(
            @PathVariable Long resumeId,
            @PathVariable Long educationId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddEducationRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                educationService.updateEducation(
                        educationId,
                        resumeId,
                        candidateId,
                        request
                )
        );
    }

    @DeleteMapping("/{educationId}")
    public ResponseEntity<ApiResponse> deleteEducation(
            @PathVariable Long resumeId,
            @PathVariable Long educationId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        educationService.deleteEducation(
                educationId,
                resumeId,
                candidateId
        );

        return ResponseEntity.ok(
                new ApiResponse(
                        "Education deleted successfully",
                        true

                )
        );
    }
}