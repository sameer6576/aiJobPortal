package com.sameer.job.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordActionResponse {
    private String message;
}
