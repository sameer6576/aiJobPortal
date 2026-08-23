package com.sameer.job.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AiTextResponse {
    private String content;
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();
}
