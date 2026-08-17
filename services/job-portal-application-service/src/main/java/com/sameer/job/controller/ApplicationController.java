package com.sameer.job.controller;

import com.sameer.job.domain.ApplicationStatus;
import com.sameer.job.dto.ApplicationResponse;
import com.sameer.job.dto.response.ApiResponse;
import com.sameer.job.payload.*;
import com.sameer.job.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // Candidate applies for a job
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody CreateApplicationRequest request
    ) throws Exception {

        ApplicationResponse response =
                applicationService.createApplication(candidateId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get application by ID
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long applicationId
    ) throws Exception {

        return ResponseEntity.ok(
                applicationService.getApplicationById(applicationId)
        );
    }

    // Get applications submitted by candidate
    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @RequestHeader("X-User-Id") Long candidateId
    ) {

        return ResponseEntity.ok(
                applicationService.getMyApplications(candidateId)
        );
    }

    // Get applications for employer's company
    @GetMapping("/company")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForCompany(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @ModelAttribute CompanyApplicationFilterRequest filter
    ) {

        return ResponseEntity.ok(
                applicationService.getApplicationsForCompany(userId, filter)
        );
    }

    // Get all applications for a job // can be skipped since getApplicationsForCompany covers this
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForJob(
            @PathVariable Long jobId
    ) {

        return ResponseEntity.ok(
                applicationService.getApplicationsForJob(jobId)
        );
    }

    // Employer updates application status
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long employerId,
            @RequestBody UpdateApplicationStatus req
    ) throws Exception {

        return ResponseEntity.ok(
                applicationService.updateStatus(
                        applicationId,
                        employerId,
                        req.getStatus()
                )
        );
    }

    // Candidate withdraws application
    @PatchMapping("/{applicationId}/withdraw")
    public ResponseEntity<ApplicationResponse> withdrawApplication(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody WithdrawApplicationRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                applicationService.withdraw(
                        applicationId,
                        candidateId,
                        request
                )
        );
    }

    // Employer stars/un-stars application
    @PatchMapping("/{applicationId}/star")
    public ResponseEntity<ApplicationResponse> toggleStar(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long employerId
    ) throws Exception {

        return ResponseEntity.ok(
                applicationService.toggleStar(
                        applicationId,
                        employerId
                )
        );
    }

    // Candidate deletes application
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<ApiResponse> deleteApplication(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        applicationService.deleteApplication(
                applicationId,
                candidateId
        );

        return ResponseEntity.ok(new ApiResponse("Application deleted successfully", true));
    }
}