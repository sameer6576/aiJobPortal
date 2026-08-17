package com.sameer.job.controller;

import com.sameer.job.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public ResponseEntity<ApiResponse> HomeController() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Service for managing candidate job preferences / saved jobs");
        apiResponse.setStatus(true);
        return ResponseEntity.ok(apiResponse);
    }
}

