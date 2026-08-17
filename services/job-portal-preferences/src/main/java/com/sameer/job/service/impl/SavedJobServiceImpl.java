package com.sameer.job.service.impl;

import com.sameer.job.dto.SavedJobResponse;
import com.sameer.job.mapper.PreferenceMapper;
import com.sameer.job.modal.SavedJob;
import com.sameer.job.payload.SaveJobRequest;
import com.sameer.job.repository.SavedJobRepository;
import com.sameer.job.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedJobServiceImpl implements SavedJobService {

    private final SavedJobRepository savedJobRepository;

    @Override
    public SavedJobResponse savedJob(Long candidateId, SaveJobRequest req) throws Exception {
        if (isSaved(candidateId, req.getJobId())) {
            throw new Exception("Job already saved");
        }

        SavedJob savedJob = SavedJob.builder()
                                    .candidateId(candidateId).jobId(req.getJobId()).build();

        savedJob= savedJobRepository.save(savedJob);

        return PreferenceMapper.toSavedJobResponse(savedJob);
    }

    @Override
    public void unsaveJob(Long candidateId, Long savedJobId) throws Exception {
        SavedJob savedJob = savedJobRepository.findById(savedJobId)
                .orElseThrow(() -> new Exception("Job not found with ID: "+ savedJobId));

        if(!savedJob.getCandidateId().equals(candidateId)){
            throw new Exception("You cannot un save this job");
        }

        savedJobRepository.delete(savedJob);

    }

    @Override
    public List<SavedJobResponse> getMySavedJob(Long candidateId) {
        return savedJobRepository.findByCandidateId(candidateId)
                .stream().map(
                        PreferenceMapper::toSavedJobResponse
                ).toList();
    }

    @Override
    public boolean isSaved(Long candidateId, Long jobId) {
        return savedJobRepository.existsByCandidateIdAndJobId(candidateId, jobId);
    }
}
