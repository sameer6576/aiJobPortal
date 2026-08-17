package com.sameer.job.mapper;

import com.sameer.job.dto.SavedJobResponse;
import com.sameer.job.modal.SavedJob;

public class PreferenceMapper {

    public static SavedJobResponse toSavedJobResponse(SavedJob savedJob){
        return SavedJobResponse.builder()
                .id(savedJob.getId()).candidateId(savedJob.getCandidateId()).jobId(savedJob.getJobId()).savedAt(savedJob.getSavedAt()).build();
    }
}
