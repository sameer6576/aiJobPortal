package com.sameer.job.controller;

import com.sameer.job.dto.ProjectResponse;
import com.sameer.job.payload.AddProjectRequest;
import com.sameer.job.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes/{resumeId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> addProject(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddProjectRequest request
    ) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(
                                     projectService.addProject(
                                             resumeId,
                                             candidateId,
                                             request
                                     )
                             );
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        return ResponseEntity.ok(
                projectService.getAllProjects(resumeId, candidateId)
        );
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long resumeId,
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") Long candidateId,
            @Valid @RequestBody AddProjectRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                projectService.updateProject(
                        projectId,
                        resumeId,
                        candidateId,
                        request
                )
        );
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long resumeId,
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") Long candidateId
    ) throws Exception {

        projectService.deleteProject(
                projectId,
                resumeId,
                candidateId
        );

        return ResponseEntity.noContent().build();
    }
}