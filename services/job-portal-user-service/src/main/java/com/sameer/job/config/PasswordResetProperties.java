package com.sameer.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.password-reset")
public class PasswordResetProperties {

    /**
     * When true, {@code POST /auth/forgot-password} includes the raw reset token
     * in the JSON body (local/demo). No SMTP is used.
     */
    private boolean exposeToken = true;

    private long expiryHours = 1;
}
