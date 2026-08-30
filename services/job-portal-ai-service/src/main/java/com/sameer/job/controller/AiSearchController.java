package com.sameer.job.controller;

import com.sameer.job.dto.ai.SearchEnhanceRequest;
import com.sameer.job.dto.ai.SearchEnhanceResponse;
import com.sameer.job.service.SearchAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiSearchController {

    private final SearchAiService searchAiService;


    @PostMapping("/search/enhance")
    public ResponseEntity<SearchEnhanceResponse> enhanceSearch(
            @Valid @RequestBody SearchEnhanceRequest request) throws Exception {
        return ResponseEntity.ok(searchAiService.enhanceSearch(request));
    }
}
