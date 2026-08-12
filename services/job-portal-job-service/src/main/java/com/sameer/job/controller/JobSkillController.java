package com.sameer.job.controller;

import com.sameer.job.dto.JobSkillResponse;
import com.sameer.job.dto.response.ApiResponse;
import com.sameer.job.payload.JobSkillRequest;
import com.sameer.job.service.JobSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job-skills")
public class JobSkillController {

    private final JobSkillService jobSkillService;

    @PostMapping
    public ResponseEntity<JobSkillResponse> createSkill(@RequestBody @Valid JobSkillRequest jobSkillRequest) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(jobSkillService.createSkill(jobSkillRequest));
    }

    @GetMapping
    public ResponseEntity<List<JobSkillResponse>> getAllSkills() throws Exception {
        return ResponseEntity.ok(jobSkillService.getAllSkills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobSkillResponse> getSkillById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(jobSkillService.getSkillById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobSkillResponse> updateSkill(@PathVariable Long id, @RequestBody @Valid JobSkillRequest jobSkillRequest) throws Exception {
        return ResponseEntity.ok(jobSkillService.updateSkills(id, jobSkillRequest));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteSkill(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(new ApiResponse("Skill deleted successfully", true));
    }


}
