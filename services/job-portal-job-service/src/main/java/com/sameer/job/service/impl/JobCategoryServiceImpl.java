package com.sameer.job.service.impl;

import com.sameer.job.dto.JobCategoryResponse;
import com.sameer.job.exception.BadRequestException;
import com.sameer.job.exception.ConflictException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.JobCategoryMapper;
import com.sameer.job.modal.JobCategory;
import com.sameer.job.payload.JobCategoryRequest;
import com.sameer.job.repository.JobCategoryRepository;
import com.sameer.job.service.JobCategoryService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;

    @Override
    public JobCategoryResponse createCategory(JobCategoryRequest req) throws Exception {
        if (jobCategoryRepository.existsByName(req.getName())) {
            throw new ConflictException("Category name already exists, choose a different name");
        }

        JobCategory parent = null;

        if (req.getParentId() != null) {
            parent = getCategoryEntityById(req.getParentId());
        }

        String slug = generateUniqueSlug(req.getName());

        JobCategory category = JobCategory.builder()
                .name(req.getName())
                .slug(slug)
                .description(req.getDescription())
                .iconUrl(req.getIconUrl())
                .parent(parent)
                .active(true)
                .build();

        JobCategory saved = jobCategoryRepository.save(category);

        return JobCategoryMapper.toJobCategoryResponse(saved,true);
    }

    @Override
    public List<JobCategoryResponse> getAllCategories() {
        return jobCategoryRepository.findByActiveTrue().stream().map(
                jobCategory -> JobCategoryMapper.toJobCategoryResponse(jobCategory, false)
        ).toList();
    }

    @Override
    public JobCategoryResponse getCategoryById(Long id) throws Exception {
        JobCategory jobCategory = getCategoryEntityById(id);
        return JobCategoryMapper.toJobCategoryResponse(jobCategory, true);
    }

    @Override
    public JobCategoryResponse updateCategory(Long id, JobCategoryRequest req) throws Exception {
        JobCategory jobCategory = getCategoryEntityById(id);
        if(!jobCategory.getName().equals(req.getName())){
            if(jobCategoryRepository.existsByName(req.getName())){
                throw new ConflictException("Category name already exist, choose a different name");
            }
        }

        JobCategory parent = null;
        if(req.getParentId()!=null){
            if(req.getParentId().equals(id)){
                throw new BadRequestException("A category can't be it's own parent");
            }
            parent = getCategoryEntityById(req.getParentId());
        }
        jobCategory.setParent(parent);
        jobCategory.setName(req.getName());
        jobCategory.setDescription(req.getDescription());
        jobCategory.setIconUrl(req.getIconUrl());
        return JobCategoryMapper.toJobCategoryResponse(jobCategoryRepository.save(jobCategory), true);
    }

    @Override
    public void deleteCategory(Long id) {
        JobCategory category = getCategoryEntityById(id);
        category.setActive(false);
        jobCategoryRepository.save(category);
    }

    @Override
    public JobCategory getCategoryEntityById(Long id) {
        return jobCategoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    private String generateUniqueSlug(@NotBlank(message = "Category name is required") String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim().replaceAll("[\\s-]", "-");
        if (!jobCategoryRepository.existsBySlug(base)) {
            return base;
        }
        int counter = 1;
        while (jobCategoryRepository.existsBySlug(base + "-" + counter)) {
            counter++;
        }
        return base + "-" + counter;
    }
}
