package com.sameer.job.service;

import com.sameer.job.domain.CompanyStatus;
import com.sameer.job.domain.CompanyType;
import com.sameer.job.domain.IndustryType;
import com.sameer.job.dto.request.CompanyRequest;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.modal.Company;

import java.util.List;

public interface CompanyService {
    CompanyResponse createCompany(Long ownerId, CompanyRequest req) throws Exception;

    CompanyResponse getCompanyById(Long id) throws Exception;

    CompanyResponse getMyCompany(Long ownerId) throws Exception;

    List<CompanyResponse> getAllCompanies(CompanyType companyType,
                                          IndustryType industryType,
                                          CompanyStatus companyStatus);

    CompanyResponse updateCompany(Long companyId, Long ownerId, CompanyRequest req) throws Exception;

    CompanyResponse verifyCompany(Long companyId) throws Exception;

    void deleteCompany(Long companyId, Long ownerId) throws Exception;

    CompanyResponse deactivateCompany(Long companyId) throws Exception;

    Company getCompanyEntityById(Long id) throws Exception;
}
