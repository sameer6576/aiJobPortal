package com.sameer.job.client;

import com.sameer.job.dto.ai.AiTextResponse;
import com.sameer.job.dto.ai.CoverLetterRequest;
import com.sameer.job.dto.ai.ScreeningScoreRequest;
import com.sameer.job.dto.ai.ScreeningScoreResponse;
import com.sameer.job.dto.ai.SkillsGapRequest;
import com.sameer.job.dto.ai.SkillsGapResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "JOB-PORTAL-AI-SERVICE")
public interface AiClient {

    @PostMapping("/api/ai/application/screening-core")
    ScreeningScoreResponse scoreCandidate(@RequestBody ScreeningScoreRequest request);

    @PostMapping("/api/ai/application/cover-letter")
    AiTextResponse generateCoverLetter(@RequestBody CoverLetterRequest request);

    @PostMapping("/api/ai/application/skills-gap")
    SkillsGapResponse analyzeSkillsGap(@RequestBody SkillsGapRequest request);
}
