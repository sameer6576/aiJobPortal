package com.sameer.job.controller;

import com.sameer.job.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public ResponseEntity<ApiResponse> HomeController() {
        String message = "Service for managing job applications, tracking application status and maintaining application history";
        ApiResponse response = new ApiResponse(message,true);
        return ResponseEntity.ok(response);
    }
}
