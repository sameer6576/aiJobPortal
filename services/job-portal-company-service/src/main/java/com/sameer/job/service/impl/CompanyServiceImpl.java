package com.sameer.job.service.impl;

import com.sameer.job.domain.CompanyStatus;
import com.sameer.job.domain.CompanyType;
import com.sameer.job.domain.IndustryType;
import com.sameer.job.dto.request.CompanyRequest;
import com.sameer.job.dto.response.CompanyResponse;
import com.sameer.job.dto.response.SocialLinkResponse;
import com.sameer.job.exception.ConflictException;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.CompanyMapper;
import com.sameer.job.modal.Company;
import com.sameer.job.modal.SocialLink;
import com.sameer.job.repository.CompanyRepository;
import com.sameer.job.service.CompanyService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public CompanyResponse createCompany(Long ownerId, CompanyRequest req) throws Exception {
        if (companyRepository.existsByOwnerId(ownerId)) {
            throw new ConflictException("You already have a company registered. Only one company per account is allowed");
        }
        if (companyRepository.existsByName(req.getName())) {
            throw new ConflictException("Company already exists. Please choose a different name.");
        }
        if (req.getRegistrationNumber() != null && companyRepository.existsByRegistrationNumber(req.getRegistrationNumber())) {
            throw new ConflictException("Company already exists. Please choose a different registration number.");
        }
        String slug = generateUniqueSlug(req.getName());

        Company company = Company.builder()
                .name(req.getName())
                .slug(slug)
                .tagline(req.getTagline())
                .description(req.getDescription())
                .logoUrl(req.getLogoUrl())
                .coverImageUrl(req.getCoverImageUrl())
                .website(req.getWebsite())
                .email(req.getEmail())
                .phone(req.getPhone())
                .foundedYear(req.getFoundedYear())
                .companySize(req.getCompanySize())
                .industryType(req.getIndustryType())
                .registrationNumber(req.getRegistrationNumber())
                .ownerId(ownerId)
                .socialLinks(mapSocialLinks(req.getSocialLinks()))
                .build();

        Company savedCompany = companyRepository.save(company);

        return CompanyMapper.toResponse(savedCompany);
    }

    private List<SocialLink> mapSocialLinks(List<SocialLinkResponse> socialLinks) {
        if (socialLinks == null || socialLinks.isEmpty()) {
            return new ArrayList<SocialLink>();
        }
        return socialLinks.stream()
                .map(link -> SocialLink.builder()
                        .platform(link.getPlatform())
                        .url(link.getUrl())
                        .build()
                ).collect(Collectors.toList());
    }

    private String generateUniqueSlug(@NotBlank(message = "Company name is required") String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim().replaceAll("[\\s-]", "-");
        if (!companyRepository.existsBySlug(base)) {
            return base;
        }
        int counter = 1;
        while (companyRepository.existsBySlug(base + "-" + counter)) {
            counter++;
        }
        return base + "-" + counter;
    }

    @Override
    public CompanyResponse getCompanyById(Long id) throws Exception {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found with ID: " + id));
        return CompanyMapper.toResponse(company);
    }

    @Override
    public CompanyResponse getMyCompany(Long ownerId) throws Exception {
        Company company = companyRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new NotFoundException("Company not found for owner: " + ownerId));
        return CompanyMapper.toResponse(company);
    }

    @Override
    public List<CompanyResponse> getAllCompanies(CompanyType companyType,
                                                 IndustryType industryType,
                                                 CompanyStatus companyStatus) {
        return companyRepository.findByFilters(companyType, industryType, companyStatus).stream()
                .map(CompanyMapper::toResponse)
                .toList();
    }

    @Override
    public CompanyResponse updateCompany(Long companyId, Long ownerId, CompanyRequest req) throws Exception {
        Company company = getCompanyEntityById(companyId);
        assertOwner(company, ownerId);

        if (!company.getName().equals(req.getName()) && companyRepository.existsByName(req.getName())) {
            throw new ConflictException("Company already exists. Please choose a different name.");
        }

        if (req.getRegistrationNumber() != null
                && !req.getRegistrationNumber().equals(company.getRegistrationNumber())
                && companyRepository.existsByRegistrationNumber(req.getRegistrationNumber())
        ) {
            throw new ConflictException("Company already exists. Please choose a different registration number");
        }

        company.setName(req.getName());
        company.setTagline(req.getTagline());
        company.setDescription(req.getDescription());
        company.setLogoUrl(req.getLogoUrl());
        company.setCoverImageUrl(req.getCoverImageUrl());
        company.setWebsite(req.getWebsite());
        company.setFoundedYear(req.getFoundedYear());
        company.setEmail(req.getEmail());
        company.setPhone(req.getPhone());
        company.setCompanySize(req.getCompanySize());
        company.setCompanyType(req.getCompanyType());
        company.setIndustryType(req.getIndustryType());
        company.setRegistrationNumber(req.getRegistrationNumber());
        company.setSocialLinks(mapSocialLinks(req.getSocialLinks()));

        Company updatedCompany = companyRepository.save(company);

        return CompanyMapper.toResponse(updatedCompany);
    }

    @Override
    public CompanyResponse verifyCompany(Long companyId) throws Exception {
        Company company = getCompanyEntityById(companyId);
        company.setStatus(CompanyStatus.ACTIVE);
        company.setVerified(true);
        return CompanyMapper.toResponse(companyRepository.save(company));
    }

    @Override
    public void deleteCompany(Long companyId, Long ownerId) throws Exception {
        Company company = getCompanyEntityById(companyId);
        assertOwner(company, ownerId);
        companyRepository.delete(company);

    }

    private void assertOwner(Company company, Long ownerId) throws Exception {
        if(!company.getOwnerId().equals(ownerId)){
            throw new ForbiddenException("You are not the owner of this company");
        }
    }

    @Override
    public CompanyResponse deactivateCompany(Long companyId) throws Exception {
        Company company = getCompanyEntityById(companyId);
        company.setStatus(CompanyStatus.SUSPENDED);
        company.setVerified(false);
        return CompanyMapper.toResponse(companyRepository.save(company));
    }

    @Override
    public Company getCompanyEntityById(Long id) throws Exception {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found with ID: " + id));
    }
}
