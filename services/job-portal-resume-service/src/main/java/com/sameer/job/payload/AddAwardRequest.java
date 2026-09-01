package com.sameer.job.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAwardRequest {
    @NotBlank(message = "Award title is required")
    @Size(max = 150, message = "Award title must not exceed 150 characters")
    private String title;

    @Size(max = 200, message = "Issued by must not exceed 200 characters")
    private String issuedBy;

    private LocalDate awardDate;

    private String description;

    private Integer displayOrder;
}
