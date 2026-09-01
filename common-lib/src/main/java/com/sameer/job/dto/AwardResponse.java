package com.sameer.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwardResponse {
    private Long id;
    private String title;
    private String issuedBy;
    private LocalDate awardDate;
    private String description;
    private Integer displayOrder;
}
