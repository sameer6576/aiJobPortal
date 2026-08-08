package com.sameer.job.dto.response;

import com.sameer.job.domain.SocialPlatform;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SocialLinkResponse {
    private SocialPlatform platform;
    private String url;
}

