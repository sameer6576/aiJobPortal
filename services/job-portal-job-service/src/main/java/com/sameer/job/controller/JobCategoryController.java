package com.sameer.job.controller;

import com.sameer.job.dto.JobCategoryResponse;
import com.sameer.job.dto.response.ApiResponse;
import com.sameer.job.payload.JobCategoryRequest;
import com.sameer.job.repository.JobCategoryRepository;
import com.sameer.job.service.JobCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/job-categories")
public class JobCategoryController {

    private final JobCategoryService jobCategoryService;

    @PostMapping
    public ResponseEntity<JobCategoryResponse> createCategory(@RequestBody @Valid JobCategoryRequest jobCategoryRequest) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(jobCategoryService.createCategory(jobCategoryRequest));
    }

    @GetMapping
    public ResponseEntity<List<JobCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(jobCategoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobCategoryResponse> getCategoryById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(jobCategoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobCategoryResponse> updateCategory(@PathVariable Long id, @RequestBody JobCategoryRequest req) throws Exception {
        return ResponseEntity.ok(jobCategoryService.updateCategory(id,req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) throws Exception {
        jobCategoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse("Category delete successfully", true));
    }

}
