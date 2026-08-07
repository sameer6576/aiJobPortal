package com.sameer.job.controller;

import com.sameer.job.domain.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public String home(){
        return "Service for managing company profiles and documents - "+ UserRole.ROLE_EMPLOYER;
    }
}
