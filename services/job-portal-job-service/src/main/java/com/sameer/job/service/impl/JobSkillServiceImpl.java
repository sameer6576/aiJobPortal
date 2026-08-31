package com.sameer.job.service.impl;

import com.sameer.job.dto.JobSkillResponse;
import com.sameer.job.exception.ConflictException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.JobSkillMapper;
import com.sameer.job.modal.JobSkill;
import com.sameer.job.payload.JobSkillRequest;
import com.sameer.job.repository.JobSkillRepository;
import com.sameer.job.service.JobSkillService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class JobSkillServiceImpl implements JobSkillService {

    private final JobSkillRepository jobSkillRepository;

    @Override
    public JobSkillResponse createSkill(JobSkillRequest req) throws Exception {
        if (jobSkillRepository.existsByName(req.getName())) {
            throw new ConflictException("Skill name already exists");
        }
        String slug = generateUniqueSlug(req.getName());

        JobSkill jobSkill = JobSkill.builder()
                                    .name(req.getName()).slug(slug)
                                    .category(req.getCategory())
                                    .active(true)
                                    .build();

        JobSkill savedSkill = jobSkillRepository.save(jobSkill);

        return JobSkillMapper.toJobSkillResponse(savedSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobSkillResponse> getAllSkills() {
        return jobSkillRepository.findByActiveTrue().stream().map(
                JobSkillMapper::toJobSkillResponse
        ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobSkillResponse getSkillById(Long id) throws Exception {
        JobSkill jobSkill = jobSkillRepository.findById(id)
                                              .orElseThrow(() -> new NotFoundException("Job skill not found with ID: " + id));

        return JobSkillMapper.toJobSkillResponse(jobSkill);

    }

    @Override
    public JobSkillResponse updateSkills(Long id, JobSkillRequest req) throws Exception {
        JobSkill jobSkill = jobSkillRepository.findById(id)
                                              .orElseThrow(() -> new NotFoundException("Job skill not found with ID: " + id));

        if (!jobSkill.getName().equals(req.getName())) {
            if (jobSkillRepository.existsByName(jobSkill.getName())) {
                throw new ConflictException("Job skill name already exists");
            }
        }
        jobSkill.setName(req.getName());
        jobSkill.setCategory(req.getCategory());
        JobSkill updated = jobSkillRepository.save(jobSkill);
        return JobSkillMapper.toJobSkillResponse(updated);


    }

    @Override
    public void deleteSkill(Long id) throws Exception {
        JobSkill jobSkill = jobSkillRepository.findById(id)
                                              .orElseThrow(() -> new NotFoundException("Job skill not found with ID: " + id));
        jobSkill.setActive(false);
        jobSkillRepository.save(jobSkill);
    }

    @Override
    public Set<JobSkill> getSkillByIds(Set<Long> ids) {
        return new HashSet<>(jobSkillRepository.findAllById(ids));
    }

    private String generateUniqueSlug(@NotBlank(message = "Skill name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String name) {
        String base = name.toLowerCase()
                          .replaceAll("[^a-z0-9\\s-]", "")
                          .trim().replaceAll("[\\s-]", "-");
        if (!jobSkillRepository.existsBySlug(base)) {
            return base;
        }
        int counter = 1;
        while (jobSkillRepository.existsBySlug(base + "-" + counter)) {
            counter++;
        }
        return base + "-" + counter;
    }
}
