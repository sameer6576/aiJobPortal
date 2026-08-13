package com.sameer.job.repository;

import com.sameer.job.modal.JobTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobTagRepository extends JpaRepository<JobTag, Long> {

    boolean existsByName(String name);
    boolean existsBySlug(String slug);
}
