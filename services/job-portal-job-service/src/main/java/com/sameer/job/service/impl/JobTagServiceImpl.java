package com.sameer.job.service.impl;

import com.sameer.job.dto.JobTagResponse;
import com.sameer.job.exception.ConflictException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.JobTagMapper;
import com.sameer.job.modal.JobTag;
import com.sameer.job.payload.JobTagRequest;
import com.sameer.job.repository.JobTagRepository;
import com.sameer.job.service.JobTagService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class JobTagServiceImpl implements JobTagService {

    private final JobTagRepository jobTagRepository;

    @Override
    public JobTagResponse createTag(JobTagRequest jobTagRequest) throws Exception {
        if (jobTagRepository.existsByName(jobTagRequest.getName())) {
            throw new ConflictException("Tag name already exists");
        }
        String slug = generateUniqueSlug(jobTagRequest.getName());
        JobTag jobTag = JobTag.builder()
                              .name(jobTagRequest.getName())
                              .slug(slug).build();
        JobTag saved = jobTagRepository.save(jobTag);
        return JobTagMapper.toJobTagResponse(saved);
    }

    private String generateUniqueSlug(@NotBlank(message = "Tag name is required") String name) {
        String base = name.toLowerCase()
                          .replaceAll("[^a-z0-9\\s-]", "")
                          .trim().replaceAll("[\\s-]", "-");
        if (!jobTagRepository.existsBySlug(base)) {
            return base;
        }
        int counter = 1;
        while (jobTagRepository.existsBySlug(base + "-" + counter)) {
            counter++;
        }
        return base + "-" + counter;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobTagResponse> getAllTags() {
        return jobTagRepository.findAll()
                               .stream().map(JobTagMapper::toJobTagResponse)
                               .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobTagResponse getById(Long id) throws Exception {
        JobTag jobTag = getTagEntityById(id);
        return JobTagMapper.toJobTagResponse(jobTag);
    }

    @Override
    public JobTagResponse updateTag(Long id, JobTagRequest jobTagRequest) throws Exception {
        JobTag jobTag = getTagEntityById(id);
        if (!jobTag.getName().equals(jobTagRequest.getName())) {
            if (jobTagRepository.existsByName(jobTagRequest.getName())) {
                throw new ConflictException("Tag name already exists");
            }
        }
        jobTag.setName(jobTagRequest.getName());
        JobTag saved = jobTagRepository.save(jobTag);
        return JobTagMapper.toJobTagResponse(saved);
    }

    @Override
    public void deleteTag(Long id) throws Exception {
        JobTag jobTag = getTagEntityById(id);
        jobTagRepository.delete(jobTag);
    }

    @Override
    public JobTag getTagEntityById(Long id) throws Exception {
        return jobTagRepository.findById(id)
                               .orElseThrow(() -> new NotFoundException("Job tag not found with ID: " + id));
    }

    @Override
    public Set<JobTag> getTagsByIds(Set<Long> ids) throws Exception {
        return new HashSet<>(jobTagRepository.findAllById(ids));
    }
}
