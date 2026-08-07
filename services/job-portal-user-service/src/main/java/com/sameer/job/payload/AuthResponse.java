package com.sameer.job.payload;

import com.sameer.job.dto.response.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String jwt;
    private String title;
    private String message;
    private UserResponse user;
}
