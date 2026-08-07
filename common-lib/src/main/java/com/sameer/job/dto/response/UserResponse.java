package com.sameer.job.dto.response;

import com.sameer.job.domain.UserRole;
import com.sameer.job.domain.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String profileImage;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

}
