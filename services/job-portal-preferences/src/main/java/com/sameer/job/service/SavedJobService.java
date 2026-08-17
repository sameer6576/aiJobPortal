package com.sameer.job.service;

import com.sameer.job.dto.SavedJobResponse;
import com.sameer.job.payload.SaveJobRequest;

import java.util.List;

public interface SavedJobService {

    SavedJobResponse savedJob(Long candidateId, SaveJobRequest req) throws Exception;

    void unsaveJob(Long candidateId, Long savedJobId) throws Exception;

    List<SavedJobResponse> getMySavedJob(Long candidateId);

    boolean isSaved(Long candidateId, Long jobId);
}
