package com.sameer.job.controller;

import com.sameer.job.payload.AuthResponse;
import com.sameer.job.payload.ForgotPasswordRequest;
import com.sameer.job.payload.ForgotPasswordResponse;
import com.sameer.job.payload.LoginRequest;
import com.sameer.job.payload.PasswordActionResponse;
import com.sameer.job.payload.ResetPasswordRequest;
import com.sameer.job.payload.SignupRequest;
import com.sameer.job.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @RequestBody @Valid SignupRequest req
    ) throws Exception {
        return ResponseEntity.ok(authService.signup(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest req
    ) throws Exception {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest req
    ) {
        return ResponseEntity.ok(authService.forgotPassword(req));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordActionResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest req
    ) {
        return ResponseEntity.ok(authService.resetPassword(req));
    }
}
