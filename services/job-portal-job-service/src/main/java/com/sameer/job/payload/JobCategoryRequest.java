package com.sameer.job.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private String iconUrl;

    private Long parentId;
}
