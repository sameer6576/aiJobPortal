package com.sameer.job.service;

import com.sameer.job.dto.JobTagResponse;
import com.sameer.job.modal.JobTag;
import com.sameer.job.payload.JobTagRequest;

import java.util.List;
import java.util.Set;

public interface JobTagService {
    JobTagResponse createTag(JobTagRequest jobTagRequest) throws Exception;

    List<JobTagResponse> getAllTags();

    JobTagResponse getById(Long id) throws Exception;

    JobTagResponse updateTag(Long id, JobTagRequest jobTagRequest) throws Exception;

    void deleteTag(Long id) throws Exception;

    JobTag getTagEntityById(Long id) throws Exception;

    Set<JobTag> getTagsByIds(Set<Long> ids) throws Exception;

}
