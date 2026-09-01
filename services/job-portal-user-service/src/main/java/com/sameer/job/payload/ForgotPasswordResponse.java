package com.sameer.job.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForgotPasswordResponse {
    private String message;
    /** Present only when {@code app.password-reset.expose-token} is true and a token was issued. */
    private String resetToken;
}
