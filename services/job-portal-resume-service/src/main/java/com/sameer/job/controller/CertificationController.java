package com.sameer.job.controller;

import com.sameer.job.dto.CertificationResponse;
import com.sameer.job.payload.AddCertificationRequest;
import com.sameer.job.service.CertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes/{resumeId}/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;

    @PostMapping
    public ResponseEntity<CertificationResponse> addCertification(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddCertificationRequest request
    ) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(
                                     certificationService.addCertification(
                                             resumeId,
                                             candidateId,
                                             request
                                     )
                             );
    }

    @GetMapping
    public ResponseEntity<List<CertificationResponse>> getCertifications(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        return ResponseEntity.ok(
                certificationService.getCertifications(resumeId, candidateId)
        );
    }

    @PutMapping("/{certificationId}")
    public ResponseEntity<CertificationResponse> updateCertification(
            @PathVariable Long resumeId,
            @PathVariable Long certificationId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddCertificationRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                certificationService.updateCertification(
                        certificationId,
                        resumeId,
                        candidateId,
                        request
                )
        );
    }

    @DeleteMapping("/{certificationId}")
    public ResponseEntity<Void> deleteCertification(
            @PathVariable Long resumeId,
            @PathVariable Long certificationId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        certificationService.deleteCertification(
                certificationId,
                resumeId,
                candidateId
        );

        return ResponseEntity.noContent().build();
    }
}
