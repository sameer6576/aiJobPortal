package com.sameer.job.payload;

import com.sameer.job.domain.ResumeTemplate;
import com.sameer.job.domain.ResumeVisibility;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateResumeRequest {
    @NotBlank(message = "Resume title is required")
    private String title;
    private ResumeTemplate template;
    private ResumeVisibility visibility;
    private Boolean isDefault;
}
