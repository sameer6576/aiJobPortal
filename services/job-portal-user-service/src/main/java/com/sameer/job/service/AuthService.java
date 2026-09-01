package com.sameer.job.service;

import com.sameer.job.payload.AuthResponse;
import com.sameer.job.payload.ForgotPasswordRequest;
import com.sameer.job.payload.ForgotPasswordResponse;
import com.sameer.job.payload.LoginRequest;
import com.sameer.job.payload.PasswordActionResponse;
import com.sameer.job.payload.ResetPasswordRequest;
import com.sameer.job.payload.SignupRequest;

public interface AuthService {

    AuthResponse signup(SignupRequest req) throws Exception;

    AuthResponse login(LoginRequest req) throws Exception;

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req);

    PasswordActionResponse resetPassword(ResetPasswordRequest req);
}
