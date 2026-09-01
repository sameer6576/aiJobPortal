package com.sameer.job.service;

import com.sameer.job.dto.AwardResponse;
import com.sameer.job.payload.AddAwardRequest;

import java.util.List;

public interface AwardService {
    AwardResponse addAward(Long resumeId, Long candidateId, AddAwardRequest request) throws Exception;

    List<AwardResponse> getAwards(Long resumeId, Long candidateId) throws Exception;

    AwardResponse updateAward(Long awardId, Long resumeId, Long candidateId, AddAwardRequest request) throws Exception;

    void deleteAward(Long awardId, Long resumeId, Long candidateId) throws Exception;
}
