package com.sameer.job.repository;

import com.sameer.job.domain.CompanyStatus;
import com.sameer.job.domain.CompanyType;
import com.sameer.job.domain.IndustryType;
import com.sameer.job.modal.Company;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @EntityGraph(attributePaths = "socialLinks")
    @Override
    Optional<Company> findById(Long id);

    @EntityGraph(attributePaths = "socialLinks")
    Optional<Company> findByOwnerId(Long ownerId);

    boolean existsByOwnerId(Long ownerId);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByRegistrationNumber(String registrationNumber);

    @EntityGraph(attributePaths = "socialLinks")
    @Query("select c from Company c where" +
            "(:companyType Is NULL OR c.companyType=:companyType) AND" +
            "(:industryType Is NULL OR c.industryType = :industryType) AND" +
            "(:status IS NULL OR c.status = :status)"
    )
    List<Company> findByFilters(
            @Param("companyType") CompanyType companyType,
            @Param("industryType") IndustryType industryType,
            @Param("status") CompanyStatus status
    );
}
