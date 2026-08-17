package com.sameer.job.controller;

import com.sameer.job.dto.SavedJobResponse;
import com.sameer.job.dto.response.ApiResponse;
import com.sameer.job.payload.SaveJobRequest;
import com.sameer.job.service.SavedJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferences/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService savedJobService;

    @PostMapping
    public ResponseEntity<SavedJobResponse> saveJob(
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody SaveJobRequest req
    ) throws Exception {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedJobService.savedJob(candidateId, req));
    }

    @DeleteMapping("/{savedJobId}")
    public ResponseEntity<ApiResponse> unsaveJob(
            @RequestHeader("X-User-Id") Long candidateId,
            @PathVariable Long savedJobId
    ) throws Exception {

        savedJobService.unsaveJob(candidateId, savedJobId);

        return ResponseEntity.ok(new ApiResponse("Job unsaved successfully", true));
    }

    @GetMapping
    public ResponseEntity<List<SavedJobResponse>> getMySavedJobs(
            @RequestHeader("X-User-Id") Long candidateId
    ) {

        return ResponseEntity.ok(
                savedJobService.getMySavedJob(candidateId)
        );
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isSaved(
            @RequestHeader("X-User-Id") Long candidateId,
            @RequestParam Long jobId
    ) {

        return ResponseEntity.ok(
                savedJobService.isSaved(candidateId, jobId)
        );
    }
}