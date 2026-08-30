package com.sameer.job.payload;

import com.sameer.job.domain.ExperienceLevel;
import com.sameer.job.domain.JobType;
import com.sameer.job.domain.WorkMode;
import com.sameer.job.dto.ai.SearchEnhanceResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class NaturalLanguageSearchMapper {

    private NaturalLanguageSearchMapper() {
    }

    public static JobSearchRequest toJobSearchRequest(SearchEnhanceResponse enhanced) {
        JobSearchRequest request = new JobSearchRequest();
        if (enhanced == null) {
            return request;
        }
        if (enhanced.getKeywords() != null && !enhanced.getKeywords().isEmpty()) {
            request.setKeyword(String.join(" ", enhanced.getKeywords()));
        }
        if (enhanced.getLocations() != null && !enhanced.getLocations().isEmpty()) {
            request.setLocation(enhanced.getLocations().getFirst());
        }
        request.setJobType(firstEnum(enhanced.getJobTypes(), JobType.class));
        request.setWorkMode(firstEnum(enhanced.getWorkModes(), WorkMode.class));
        request.setExperienceLevel(mapExperience(enhanced.getExperienceLevels()));
        if (enhanced.getMinSalary() != null) {
            request.setMinSalary(BigDecimal.valueOf(enhanced.getMinSalary()));
        }
        return request;
    }

    private static ExperienceLevel mapExperience(List<String> values) {
        if (values == null || values.isEmpty() || values.getFirst() == null) {
            return null;
        }
        String raw = values.getFirst().trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (raw) {
            case "ENTRY", "ENTRY_LEVEL" -> ExperienceLevel.ENTRY_LEVEL;
            case "JUNIOR" -> ExperienceLevel.JUNIOR;
            case "MID", "MID_LEVEL" -> ExperienceLevel.MID_LEVEL;
            case "SENIOR", "SENIOR_LEVEL" -> ExperienceLevel.SENIOR_LEVEL;
            case "LEAD" -> ExperienceLevel.LEAD;
            case "EXECUTIVE" -> ExperienceLevel.EXECUTIVE;
            default -> firstEnum(List.of(raw), ExperienceLevel.class);
        };
    }

    private static <E extends Enum<E>> E firstEnum(List<String> values, Class<E> type) {
        if (values == null || values.isEmpty() || values.getFirst() == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, values.getFirst().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
