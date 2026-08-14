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
        apiResponse.setMessage("Service for managing candidate resumes, including resume builder, \n" +
                "\t\tmultiple versions, and resume parsing");
        apiResponse.setStatus(true);
        return ResponseEntity.ok(apiResponse);
    }
}
