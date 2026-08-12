package com.sameer.job.service;

import com.sameer.job.dto.JobCategoryResponse;
import com.sameer.job.modal.JobCategory;
import com.sameer.job.payload.JobCategoryRequest;

import java.util.List;

public interface JobCategoryService {

    JobCategoryResponse createCategory(JobCategoryRequest req) throws Exception;

    List<JobCategoryResponse> getAllCategories();

    JobCategoryResponse getCategoryById(Long id) throws Exception;

    JobCategoryResponse updateCategory(Long id, JobCategoryRequest req) throws Exception;

    void deleteCategory(Long id);

    JobCategory getCategoryEntityById(Long id);
}
