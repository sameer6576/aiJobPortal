package com.sameer.job.repository;

import com.sameer.job.modal.JobCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"parent"})
    List<JobCategory> findByActiveTrue();

    @EntityGraph(attributePaths = {"parent", "subCategories"})
    @Override
    Optional<JobCategory> findById(Long id);

}
