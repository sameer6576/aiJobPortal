package com.sameer.job.modal;

import com.sameer.job.domain.SocialPlatform;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SocialLink {
    private SocialPlatform platform;
    private String url;
}
