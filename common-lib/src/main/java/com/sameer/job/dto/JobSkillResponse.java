package com.sameer.job.dto;

import com.sameer.job.domain.SkillCategory;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSkillResponse {
    private Long id;
    private String name;
    private String slug;
    private SkillCategory category;
    private Boolean active;
}
