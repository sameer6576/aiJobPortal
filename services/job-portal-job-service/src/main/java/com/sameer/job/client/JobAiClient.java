package com.sameer.job.client;

import com.sameer.job.dto.ai.SearchEnhanceRequest;
import com.sameer.job.dto.ai.SearchEnhanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "JOB-PORTAL-AI-SERVICE")
public interface JobAiClient {

    @PostMapping("/api/ai/search/enhance")
    SearchEnhanceResponse enhanceSearch(@RequestBody SearchEnhanceRequest request);
}
