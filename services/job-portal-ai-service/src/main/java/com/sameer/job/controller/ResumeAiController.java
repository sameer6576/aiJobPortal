package com.sameer.job.controller;

import com.sameer.job.dto.ai.AiTextResponse;
import com.sameer.job.payload.*;
import com.sameer.job.service.ResumeAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/resume")
public class ResumeAiController {

    private final ResumeAiService resumeAiService;

    @PostMapping("/summary")
    public ResponseEntity<AiTextResponse> generateSummary(
            @Valid @RequestBody ResumeSummaryRequest resumeSummaryRequest
    ) throws Exception {
        AiTextResponse response = resumeAiService.generateProfessionalSummary(resumeSummaryRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/experience-bullets")
    public ResponseEntity<WorkExperienceBulletsResponse> generateBullets(
            @Valid @RequestBody
            WorkExperienceBulletRequest workExperienceBulletRequest
    ) throws Exception {
        WorkExperienceBulletsResponse workExperienceBulletsResponse = resumeAiService.generateWorkExperienceBullets(workExperienceBulletRequest);
        return ResponseEntity.ok(workExperienceBulletsResponse);
    }

    @PostMapping("/improvements")
    public ResponseEntity<ResumeImprovementResponse> getImprovements(
            @Valid @RequestBody
            ResumeImprovementRequest request
    ) throws Exception {
        ResumeImprovementResponse resumeImprovementResponse = resumeAiService.getResumeImprovementTips(request);
        return ResponseEntity.ok(resumeImprovementResponse);
    }

    @PostMapping("/career-feedback")
    public ResponseEntity<CareerFeedbackResponse> generateCareerFeedback(
            @Valid @RequestBody
            CareerFeedbackRequest request
    ) throws Exception {
        CareerFeedbackResponse careerFeedbackResponse = resumeAiService.generateCareerFeedback(request);
        return ResponseEntity.ok(careerFeedbackResponse);
    }
}
