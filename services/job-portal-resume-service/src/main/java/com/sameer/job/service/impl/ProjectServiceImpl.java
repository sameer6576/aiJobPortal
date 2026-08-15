package com.sameer.job.service.impl;

import com.sameer.job.dto.ProjectResponse;
import com.sameer.job.mapper.ResumeMapper;
import com.sameer.job.modal.Project;
import com.sameer.job.modal.Resume;
import com.sameer.job.payload.AddProjectRequest;
import com.sameer.job.repository.ProjectRepository;
import com.sameer.job.service.ProjectService;
import com.sameer.job.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ResumeService resumeService;

    @Override
    @Transactional
    public ProjectResponse addProject(
            Long resumeId,
            Long candidateId,
            AddProjectRequest req
    ) throws Exception {

        Resume resume = resumeService.getResumeEntity(resumeId);
        assertOwner(resume, candidateId);

        Project project = Project.builder()
                                 .resume(resume)
                                 .title(req.getTitle())
                                 .description(req.getDescription())
                                 .technologies(req.getTechnologies() != null
                                         ? new ArrayList<>(req.getTechnologies())
                                         : new ArrayList<>())
                                 .projectUrl(req.getProjectUrl())
                                 .sourceCodeUrl(req.getSourceCodeUrl())
                                 .startDate(req.getStartDate())
                                 .endDate(req.getEndDate())
                                 .isOngoing(Boolean.TRUE.equals(req.getIsOngoing()))
                                 .displayOrder(req.getDisplayOrder() != null
                                         ? req.getDisplayOrder()
                                         : 0)
                                 .build();

        Project saved = projectRepository.save(project);

        return ResumeMapper.toProjectResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(Long resumeId) {

        return projectRepository
                .findByResume_IdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .map(ResumeMapper::toProjectResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(
            Long projectId,
            Long resumeId,
            Long candidateId,
            AddProjectRequest req
    ) throws Exception {

        Project project = getOwnedProject(
                projectId,
                resumeId,
                candidateId
        );

        project.setTitle(req.getTitle());
        project.setDescription(req.getDescription());
        project.setProjectUrl(req.getProjectUrl());
        project.setSourceCodeUrl(req.getSourceCodeUrl());
        project.setStartDate(req.getStartDate());
        project.setEndDate(req.getEndDate());
        project.setIsOngoing(
                Boolean.TRUE.equals(req.getIsOngoing())
        );

        if (req.getTechnologies() != null) {
            project.setTechnologies(
                    new ArrayList<>(req.getTechnologies())
            );
        }

        if (req.getDisplayOrder() != null) {
            project.setDisplayOrder(req.getDisplayOrder());
        }

        Project saved = projectRepository.save(project);

        return ResumeMapper.toProjectResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProject(
            Long projectId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Project project = getOwnedProject(
                projectId,
                resumeId,
                candidateId
        );

        projectRepository.delete(project);
    }

    private Project getOwnedProject(
            Long projectId,
            Long resumeId,
            Long candidateId
    ) throws Exception {

        Project project = projectRepository.findById(projectId)
                                           .orElseThrow(() ->
                                                   new Exception(
                                                           "Project not found with ID: " + projectId
                                                   )
                                           );

        if (!project.getResume().getId().equals(resumeId)) {
            throw new Exception(
                    "Project does not belong to this resume"
            );
        }

        assertOwner(project.getResume(), candidateId);

        return project;
    }

    private void assertOwner(
            Resume resume,
            Long candidateId
    ) throws Exception {

        if (!resume.getCandidateId().equals(candidateId)) {
            throw new Exception("Resume not found");
        }
    }
}