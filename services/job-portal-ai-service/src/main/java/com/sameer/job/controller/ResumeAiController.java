package com.sameer.job.controller;

import com.sameer.job.dto.ai.AiTextResponse;
import com.sameer.job.payload.*;
import com.sameer.job.service.ResumeAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/resume")
public class ResumeAiController {

    private final ResumeAiService resumeAiService;

    @GetMapping("/summary")
    public ResponseEntity<AiTextResponse> generateSummary(
            @RequestBody ResumeSummaryRequest resumeSummaryRequest
    ) throws Exception {
        AiTextResponse response = resumeAiService.generateProfessionalSummary(resumeSummaryRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/experience-bullets")
    public ResponseEntity<WorkExperienceBulletsResponse> generateBullets(
            @RequestBody
            WorkExperienceBulletRequest workExperienceBulletRequest
    ) throws Exception {
        WorkExperienceBulletsResponse workExperienceBulletsResponse = resumeAiService.generateWorkExperienceBullets(workExperienceBulletRequest);
        return ResponseEntity.ok(workExperienceBulletsResponse);
    }

    @GetMapping("/improvements")
    public ResponseEntity<ResumeImprovementResponse> getImprovements(
            @RequestBody
            ResumeImprovementRequest request
    ) throws Exception {
        ResumeImprovementResponse resumeImprovementResponse = resumeAiService.getResumeImprovementTips(request);
        return ResponseEntity.ok(resumeImprovementResponse);
    }

    @GetMapping("/career-feedback")
    public ResponseEntity<CareerFeedbackResponse> generateCareerFeedback(
            @RequestBody
            CareerFeedbackRequest request
    ) throws Exception {
        CareerFeedbackResponse careerFeedbackResponse = resumeAiService.generateCareerFeedback(request);
        return ResponseEntity.ok(careerFeedbackResponse);
    }
}
