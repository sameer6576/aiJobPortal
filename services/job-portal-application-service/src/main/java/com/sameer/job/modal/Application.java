package com.sameer.job.modal;

import com.sameer.job.domain.AiShortListStatus;
import com.sameer.job.domain.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long candidateId;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long employerId;

    @Column(nullable = false)
    private Long resumeId;

    @Column(nullable = false)
    private Long jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    private String coverLetter;

    private BigDecimal expectedSalary;

    private LocalDate availableFrom;

    private Boolean isStarred = false;

    private Integer aiScore;

    @Enumerated(EnumType.STRING)
    private AiShortListStatus aiShortListStatus;

    private LocalDateTime withdrawnAt;

    private String withdrawnReason;

    private LocalDateTime appliedAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;


}
