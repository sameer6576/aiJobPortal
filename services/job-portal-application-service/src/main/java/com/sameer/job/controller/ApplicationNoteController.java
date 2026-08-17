package com.sameer.job.controller;

import com.sameer.job.dto.ApplicationNoteResponse;
import com.sameer.job.dto.response.ApiResponse;
import com.sameer.job.payload.AddApplicationNoteRequest;
import com.sameer.job.service.ApplicationNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/notes")
@RequiredArgsConstructor
public class ApplicationNoteController {

    private final ApplicationNoteService applicationNoteService;

    @PostMapping
    public ResponseEntity<ApplicationNoteResponse> addNote(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long employerId,
            @Valid @RequestBody AddApplicationNoteRequest request
    ) throws Exception {

        ApplicationNoteResponse response =
                applicationNoteService.addNote(
                        applicationId,
                        employerId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationNoteResponse>> getNotes(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long employerId
    ) {

        return ResponseEntity.ok(
                applicationNoteService.getNotesByApplication(
                        applicationId,
                        employerId
                )
        );
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<ApiResponse> deleteNote(
            @PathVariable Long applicationId,
            @PathVariable Long noteId,
            @RequestHeader("X-User-Id") Long employerId
    ) throws Exception {

        applicationNoteService.deleteNote(
                applicationId,
                noteId,
                employerId
        );

        return ResponseEntity.ok(new ApiResponse("Note deleted successfully", true));
    }
}