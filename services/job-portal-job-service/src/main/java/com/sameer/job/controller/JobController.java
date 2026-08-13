package com.sameer.job.controller;

import com.sameer.job.dto.JobRequest;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.response.ApiResponse;
import com.sameer.job.payload.JobSearchRequest;
import com.sameer.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @RequestHeader("X-User-Id") Long employerId,
            @RequestBody @Valid JobRequest jobRequest) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.createJob(employerId, jobRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable Long id) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.getJobById(id));
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getJobs(
            @ModelAttribute JobSearchRequest jobSearchRequest
    ) throws Exception {
        return ResponseEntity.ok(jobService.getJobs(jobSearchRequest));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<JobResponse>> getJobsByCompany(
            @PathVariable Long companyId
    ) throws Exception {
        return ResponseEntity.ok(jobService.getJobsByCompany(companyId));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<JobResponse>> getAllJobsAdmin() throws Exception {
        return ResponseEntity.ok(jobService.getAllJobsAdmin());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @RequestHeader ("X-User-Id") Long employerId,
            @RequestBody @Valid JobRequest req
    ) throws Exception {
        return ResponseEntity.ok(jobService.updateJob(id,employerId,req));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<JobResponse> publishJob(
            @PathVariable Long id,
            @RequestHeader ("X-User-Id") Long employerId
    ) throws Exception {
        return ResponseEntity.ok(jobService.publishJob(id,employerId));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<JobResponse> closeJob(
            @PathVariable Long id,
            @RequestHeader ("X-User-Id") Long employerId
    ) throws Exception {
        return ResponseEntity.ok(jobService.closeJob(id,employerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteJob(
            @PathVariable Long id,
            @RequestHeader ("X-User-Id") Long employerId
    ) throws Exception {
        jobService.deleteJob(id,employerId);
        return ResponseEntity.ok(new ApiResponse("Job deleted successfully", true));
    }

}
