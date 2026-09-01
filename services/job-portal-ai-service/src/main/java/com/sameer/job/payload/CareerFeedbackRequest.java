package com.sameer.job.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerFeedbackRequest {
    @NotBlank(message = "Resume content is required")
    private String resumeContent;

    private String targetJobTitle;

    private String additionalContext;
}
