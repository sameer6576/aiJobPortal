package com.sameer.job.modal;

import com.sameer.job.domain.ResumeTemplate;
import com.sameer.job.domain.ResumeVisibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long candidateId;

    @Column(nullable = false, length = 150)
    private String title;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResumeTemplate template = ResumeTemplate.PROFESSIONAL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResumeVisibility visibility = ResumeVisibility.PUBLIC;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDefault = false;

    @Embedded
    private PersonalInfo personalInfo;

    private String summary;

    @Builder.Default
    private Integer completionScore = 0;

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;



}
