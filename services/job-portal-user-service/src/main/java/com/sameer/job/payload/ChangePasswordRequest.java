package com.sameer.job.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "is mandatory")
    private String currentPassword;

    @NotBlank(message = "is mandatory")
    @Size(min = 8, message = "must be at least 8 characters")
    private String newPassword;
}
