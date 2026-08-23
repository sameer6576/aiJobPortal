package com.sameer.job.service;

import com.sameer.job.client.GeminiClient;
import com.sameer.job.payload.JobAlertSuggestRequest;
import com.sameer.job.payload.JobAlertSuggestResponse;
import com.sameer.job.payload.SearchEnhanceRequest;
import com.sameer.job.payload.SearchEnhanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchAiService {

    private final GeminiClient geminiClient;

    private static final String SYSTEM_PROMPT = """
            You are a job search expert and career advisor with deep knowledge
            of the Indian job market.

            You extract structured search criteria from natural language and
            provide data-driven career recommendations.

            Always use the exact enum values specified in the prompt.
            Never invent new enum values.

            When asked for JSON, respond ONLY with valid JSON —
            no explanation and no markdown fences.
            """;

    public SearchEnhanceResponse enhanceSearch(
            SearchEnhanceRequest req
    ) throws Exception {

        String prompt = """
                Extract structured job search criteria from this natural
                language query.

                User Query:
                "%s"

                Analyze the query and extract ALL implied and explicit
                search criteria.

                Valid jobTypes:
                FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, FREELANCE

                Valid workModes:
                REMOTE, HYBRID, ON_SITE

                Valid experienceLevels:
                ENTRY, MID, SENIOR, LEAD, EXECUTIVE

                Return JSON using exactly this structure:

                {
                  "keywords": ["keyword1", "keyword2"],
                  "locations": ["city1", "city2"],
                  "jobTypes": ["FULL_TIME"],
                  "workModes": ["REMOTE"],
                  "experienceLevels": ["ENTRY"],
                  "minSalary": null,
                  "skills": ["skill1", "skill2"]
                }

                Rules:
                - Only include fields that are mentioned or clearly implied.
                - Use null for minSalary if not mentioned.
                - Use empty arrays [] for fields that are not mentioned.
                - "freshers" or "entry level" → ENTRY experience level.
                - "senior" or "5+ years" → SENIOR experience level.
                - "wfh" or "work from home" → REMOTE work mode.
                """.formatted(req.getQuery());

        return geminiClient.generateJson(
                SYSTEM_PROMPT,
                prompt,
                SearchEnhanceResponse.class
        );
    }

    public JobAlertSuggestResponse suggestJobAlertCriteria(
            JobAlertSuggestRequest req
    ) throws Exception {

        String skills = req.getSkills() != null
                ? String.join(", ", req.getSkills())
                : "Not Provided";

        String previousJobTitles = req.getPreviousJobTitles() != null
                ? String.join(", ", req.getPreviousJobTitles())
                : "Not Provided";

        String education = req.getEducations() != null
                ? String.join(", ", req.getEducations())
                : "Not Provided";

        String prompt = """
                Based on this candidate's profile, suggest optimal job alert
                criteria to find the best matching jobs.

                Candidate Profile:
                - Skills: %s
                - Experience Level: %s
                - Previous Job Titles: %s
                - Education: %s

                Valid jobTypes:
                FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, FREELANCE

                Valid workModes:
                REMOTE, HYBRID, ON_SITE

                Valid experienceLevels:
                ENTRY, MID, SENIOR, LEAD, EXECUTIVE

                Return JSON using exactly this structure:

                {
                  "suggestedKeywords": [
                    "keyword1",
                    "keyword2"
                  ],
                  "suggestedLocations": [
                    "city1",
                    "city2"
                  ],
                  "suggestedJobTypes": [
                    "FULL_TIME"
                  ],
                  "suggestedWorkModes": [
                    "REMOTE",
                    "HYBRID"
                  ],
                  "suggestedExperienceLevels": [
                    "MID"
                  ],
                  "suggestedIndustries": [
                    "Technology",
                    "Finance"
                  ],
                  "reasoning":
                    "Brief explanation of why these criteria were chosen"
                }

                Rules:
                - Base recommendations only on the candidate's profile.
                - Do not invent candidate skills, experience, or education.
                - Use only the exact enum values provided above.
                - Prefer realistic and relevant job alert criteria.
                """.formatted(
                skills,
                req.getExperienceLevel(),
                previousJobTitles,
                education
        );

        return geminiClient.generateJson(
                SYSTEM_PROMPT,
                prompt,
                JobAlertSuggestResponse.class
        );
    }
}