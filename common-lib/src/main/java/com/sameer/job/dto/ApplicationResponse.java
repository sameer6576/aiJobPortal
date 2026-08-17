package com.sameer.job.dto;

import com.sameer.job.domain.ApplicationStatus;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private UserResponse candidate;
    private Long employerId;

    private JobResponse job;
    private CompanyResponse company;
    private ApplicationStatus status;

    private Long resumeId;
    private String coverLetter;

    private BigDecimal expectedSalary;
    private LocalDate availableFrom;

    private Boolean isStarred;

    private List<ApplicationNoteResponse> notes;

    private LocalDateTime withdrawnAt;
    private String withdrawnReason;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    // Todo AI screening result
//       private ApplicationScreeningResponse screening;

}
