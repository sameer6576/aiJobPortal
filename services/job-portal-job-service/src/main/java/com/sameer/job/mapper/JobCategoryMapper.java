package com.sameer.job.mapper;

import com.sameer.job.dto.JobCategoryResponse;
import com.sameer.job.modal.JobCategory;

import java.util.List;

public class JobCategoryMapper {

    public static JobCategoryResponse toJobCategoryResponse(JobCategory jobCategory, boolean includeChildren) {

        List<JobCategoryResponse> subCategories = null;
        if (includeChildren) {
            subCategories = jobCategory.getSubCategories()
                                       .stream()
                                       .map(sub -> toJobCategoryResponse(sub, false))
                                       .toList();
        }

        return JobCategoryResponse
                .builder()
                .id(jobCategory.getId())
                .name(jobCategory.getName())
                .slug(jobCategory.getSlug())
                .description(jobCategory.getDescription())
                .iconUrl(jobCategory.getIconUrl())
                .active(jobCategory.getActive())
                .parentId(jobCategory.getParent() != null ? jobCategory.getParent().getId() : null)
                .parentName(jobCategory.getParent() != null ? jobCategory.getParent().getName() : null)
                .subCategories(subCategories)
                .createdAt(jobCategory.getCreatedAt())
                .build();
    }
}
