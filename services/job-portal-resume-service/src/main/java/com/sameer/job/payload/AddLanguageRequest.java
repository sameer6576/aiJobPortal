package com.sameer.job.payload;

import com.sameer.job.domain.LanguageProficiency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddLanguageRequest {

    @NotBlank(message = "Language name is required")
    private String languageName;

    @NotNull(message = "Proficiency level is required")
    private LanguageProficiency proficiency;

    private Integer displayOrder;
}
