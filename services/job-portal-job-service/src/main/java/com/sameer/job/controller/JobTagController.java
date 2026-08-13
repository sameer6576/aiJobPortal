package com.sameer.job.controller;

import com.sameer.job.dto.JobTagResponse;
import com.sameer.job.payload.JobTagRequest;
import com.sameer.job.service.JobTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-tags")
@RequiredArgsConstructor
public class JobTagController {

    private final JobTagService jobTagService;

    @PostMapping
    public ResponseEntity<JobTagResponse> createTag(
            @Valid @RequestBody JobTagRequest jobTagRequest
    ) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(jobTagService.createTag(jobTagRequest));
    }

    @GetMapping
    public ResponseEntity<List<JobTagResponse>> getAllTags() {

        return ResponseEntity.ok(
                jobTagService.getAllTags()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobTagResponse> getTagById(
            @PathVariable Long id
    ) throws Exception {

        return ResponseEntity.ok(
                jobTagService.getById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobTagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody JobTagRequest jobTagRequest
    ) throws Exception {

        return ResponseEntity.ok(
                jobTagService.updateTag(id, jobTagRequest)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long id
    ) throws Exception {

        jobTagService.deleteTag(id);

        return ResponseEntity.noContent().build();
    }
}