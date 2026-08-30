package com.sameer.job.controller;

import com.sameer.job.dto.ai.CoverLetterRequest;
import com.sameer.job.dto.ai.ScreeningScoreRequest;
import com.sameer.job.dto.ai.ScreeningScoreResponse;
import com.sameer.job.dto.ai.SkillsGapRequest;
import com.sameer.job.dto.ai.SkillsGapResponse;
import com.sameer.job.dto.ai.AiTextResponse;
import com.sameer.job.service.ApplicationAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/application")
@RequiredArgsConstructor
public class AiApplicationController {

    private final ApplicationAiService applicationAiService;

    @PostMapping({"/cover-letter"})
    public ResponseEntity<AiTextResponse> generateCoverLetter(@RequestBody @Valid CoverLetterRequest request) throws Exception {
        AiTextResponse response = this.applicationAiService.generateCoverLetter(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/screening-core"})
    public ResponseEntity<ScreeningScoreResponse> scoreCandidate(@RequestBody @Valid ScreeningScoreRequest request) throws Exception {
        return ResponseEntity.ok(this.applicationAiService.scoreCandidate(request));
    }

    @PostMapping({"/skills-gap"})
    public ResponseEntity<SkillsGapResponse> analyzeSkillsGap(@RequestBody @Valid SkillsGapRequest request) throws Exception {
        return ResponseEntity.ok(this.applicationAiService.analyzeSkillsGap(request));
    }
}
