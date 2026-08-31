package com.sameer.job.repository;

import com.sameer.job.modal.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @EntityGraph(attributePaths = "technologies")
    List<Project> findByResume_IdOrderByDisplayOrderAsc(Long resumeId);
}