package com.sameer.job.controller;

import com.sameer.job.dto.ResumeSkillResponse;
import com.sameer.job.payload.AddResumeSkillRequest;
import com.sameer.job.service.ResumeSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes/{resumeId}/skills")
@RequiredArgsConstructor
public class ResumeSkillController {

    private final ResumeSkillService resumeSkillService;

    @PostMapping
    public ResponseEntity<ResumeSkillResponse> addSkill(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddResumeSkillRequest request
    ) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(
                                     resumeSkillService.addSkill(
                                             resumeId,
                                             candidateId,
                                             request
                                     )
                             );
    }

    @GetMapping
    public ResponseEntity<List<ResumeSkillResponse>> getSkills(
            @PathVariable Long resumeId
    ) {

        return ResponseEntity.ok(
                resumeSkillService.getSkills(resumeId)
        );
    }

    @PutMapping("/{skillId}")
    public ResponseEntity<ResumeSkillResponse> updateSkill(
            @PathVariable Long resumeId,
            @PathVariable Long skillId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddResumeSkillRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                resumeSkillService.updateSkill(
                        skillId,
                        resumeId,
                        candidateId,
                        request
                )
        );
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long resumeId,
            @PathVariable Long skillId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        resumeSkillService.deleteSkill(
                skillId,
                resumeId,
                candidateId
        );

        return ResponseEntity.noContent().build();
    }
}