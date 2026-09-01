package com.sameer.job.repository;

import com.sameer.job.modal.Job;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    @EntityGraph(attributePaths = {"skills", "tags", "category", "category.parent"})
    @Override
    Optional<Job> findById(Long id);

    @EntityGraph(attributePaths = {"skills", "tags", "category", "category.parent"})
    List<Job> findByCompanyId(Long companyId);

    @EntityGraph(attributePaths = {"skills", "tags", "category", "category.parent"})
    List<Job> findByEmployerIdOrderByCreatedAtDescIdDesc(Long employerId);

    @EntityGraph(attributePaths = {"skills", "tags", "category", "category.parent"})
    @Override
    List<Job> findAll();

    @EntityGraph(attributePaths = {"skills", "tags", "category", "category.parent"})
    List<Job> findAll(Specification<Job> spec);
}
