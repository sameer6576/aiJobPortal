package com.sameer.job.repository;

import com.sameer.job.modal.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

    List<Application> findByCandidateId(Long candidateId);
    List<Application> findByJobId(Long jobId);
    List<Application> findByCompanyId(Long companyId);
    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

}
