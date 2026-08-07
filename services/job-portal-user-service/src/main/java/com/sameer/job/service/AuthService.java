package com.sameer.job.service;

import com.sameer.job.payload.AuthResponse;
import com.sameer.job.payload.LoginRequest;
import com.sameer.job.payload.SignupRequest;

public interface AuthService {

    AuthResponse signup(SignupRequest req) throws Exception;

    AuthResponse login(LoginRequest req) throws Exception;
}
