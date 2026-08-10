package com.sameer.job.controller;

import com.sameer.job.domain.UserRole;
import com.sameer.job.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping
    public ApiResponse HomeController(){
        return new ApiResponse("Service for managing job postings, search and filtering --- " + UserRole.ROLE_EMPLOYER, true);
    }
}
