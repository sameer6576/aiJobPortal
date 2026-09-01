package com.sameer.job.controller;

import com.sameer.job.dto.AwardResponse;
import com.sameer.job.payload.AddAwardRequest;
import com.sameer.job.service.AwardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes/{resumeId}/awards")
@RequiredArgsConstructor
public class AwardController {

    private final AwardService awardService;

    @PostMapping
    public ResponseEntity<AwardResponse> addAward(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddAwardRequest request
    ) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(
                                     awardService.addAward(
                                             resumeId,
                                             candidateId,
                                             request
                                     )
                             );
    }

    @GetMapping
    public ResponseEntity<List<AwardResponse>> getAwards(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        return ResponseEntity.ok(
                awardService.getAwards(resumeId, candidateId)
        );
    }

    @PutMapping("/{awardId}")
    public ResponseEntity<AwardResponse> updateAward(
            @PathVariable Long resumeId,
            @PathVariable Long awardId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddAwardRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                awardService.updateAward(
                        awardId,
                        resumeId,
                        candidateId,
                        request
                )
        );
    }

    @DeleteMapping("/{awardId}")
    public ResponseEntity<Void> deleteAward(
            @PathVariable Long resumeId,
            @PathVariable Long awardId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        awardService.deleteAward(
                awardId,
                resumeId,
                candidateId
        );

        return ResponseEntity.noContent().build();
    }
}
