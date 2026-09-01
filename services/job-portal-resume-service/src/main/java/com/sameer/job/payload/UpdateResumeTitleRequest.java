package com.sameer.job.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResumeTitleRequest {
    @NotBlank(message = "Resume title is required")
    @Size(max = 150, message = "Resume title must not exceed 150 characters")
    private String title;
}
