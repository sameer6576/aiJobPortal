package com.sameer.job.repository;

import com.sameer.job.domain.AiShortListStatus;
import com.sameer.job.domain.ApplicationStatus;
import com.sameer.job.modal.Application;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    public static Specification<Application> forCompanyWithFilters(
            Long companyId,
            Long jobId,
            ApplicationStatus status,
            Boolean isStarred,
            AiShortListStatus aiShortListStatus,
            Integer minAiScore
    ) {
        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));
            if (jobId != null) predicates.add(cb.equal(root.get("jobId"), jobId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (aiShortListStatus != null) predicates.add(cb.equal(root.get("aiShortListStatus"), aiShortListStatus));
            if (minAiScore != null) predicates.add(cb.greaterThanOrEqualTo(root.get("aiScore"), minAiScore));
            if (isStarred != null) {
                predicates.add(cb.equal(root.get("isStarred"), isStarred));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }
}
