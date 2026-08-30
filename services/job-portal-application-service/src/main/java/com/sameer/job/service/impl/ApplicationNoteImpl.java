package com.sameer.job.service.impl;

import com.sameer.job.dto.ApplicationNoteResponse;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.ApplicationMapper;
import com.sameer.job.modal.Application;
import com.sameer.job.modal.ApplicationNote;
import com.sameer.job.payload.AddApplicationNoteRequest;
import com.sameer.job.repository.ApplicationNoteRepository;
import com.sameer.job.repository.ApplicationRepository;
import com.sameer.job.service.ApplicationNoteService;
import com.sameer.job.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationNoteImpl implements ApplicationNoteService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final ApplicationNoteRepository applicationNoteRepository;


    @Override
    public ApplicationNoteResponse addNote(Long applicationId, Long employerId, AddApplicationNoteRequest req) throws Exception {
        Application application = applicationService.getApplicationEntity(applicationId);
        assertEmployer(application, employerId);

        ApplicationNote applicationNote = ApplicationNote.builder()
                                                         .application(application)
                                                         .addedByUserId(employerId)
                                                         .content(req.getContent())
                                                         .build();

        ApplicationNote saved = applicationNoteRepository.save(applicationNote);

        return ApplicationMapper.toNoteResponse(saved);
    }

    @Override
    public List<ApplicationNoteResponse> getNotesByApplication(Long applicationId, Long employerId) throws Exception {
        Application application = applicationService.getApplicationEntity(applicationId);
        assertEmployer(application, employerId);
        return applicationNoteRepository.findByApplicationId(applicationId).stream()
                                        .map(ApplicationMapper::toNoteResponse)
                                        .toList();
    }

    @Override
    public void deleteNote(Long applicationId, Long noteId, Long employerId) throws Exception {
        Application application = applicationService.getApplicationEntity(applicationId);
        assertEmployer(application, employerId);

        ApplicationNote applicationNote = applicationNoteRepository.findById(noteId)
                                                                   .orElseThrow(() -> new NotFoundException("Note does not exist with ID: " + noteId));
        if (!applicationNote.getApplication().getId().equals(applicationId)) {
            throw new NotFoundException("Note does not exist with ID: " + noteId);
        }

        applicationNoteRepository.delete(applicationNote);
    }


    private void assertEmployer(Application application, Long employerId) throws Exception {
        if (!application.getEmployerId().equals(employerId)) {
            throw new ForbiddenException("You are not the employer of this application");
        }
    }
}
