package com.sameer.job.event;

import com.sameer.job.client.CompanyClient;
import com.sameer.job.client.JobClient;
import com.sameer.job.client.UserClient;
import com.sameer.job.domain.ApplicationStatus;
import com.sameer.job.dto.JobResponse;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.dto.response.UserResponse;
import com.sameer.job.modal.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ApplicationEventPublisher {

    public static final String TOPIC = "application.status.changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final UserClient userClient;
    private final JobClient jobClient;
    private final CompanyClient companyClient;

    public void publishStatusChange(Application app,
                                    ApplicationStatus oldStatus,
                                    ApplicationStatus newStatus,
                                    String note) {
        try {
            UserResponse candidate = userClient.getUserById(app.getCandidateId());
            JobResponse job = jobClient.getJobById(app.getJobId());
            CompanyResponse company = companyClient.getMyCompany(app.getCompanyId());

            ApplicationStatusChangedEvent event = ApplicationStatusChangedEvent
                    .builder()
                    .applicationId(app.getId())
                    .candidateId(app.getCandidateId())
                    .candidateEmail(candidate.getEmail())
                    .candidateName(candidate.getFullName())
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .note(note)
                    .jobTitle(job.getTitle())
                    .companyName(company.getName())
                    .changedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(TOPIC, String.valueOf(app.getId()), event);
        } catch (Exception e) {
            System.out.println("Error in publish status change-----" + e.getMessage());
        }
    }
}
