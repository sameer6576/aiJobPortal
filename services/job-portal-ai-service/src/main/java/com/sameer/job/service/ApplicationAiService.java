package com.sameer.job.service;

import com.sameer.job.client.GeminiClient;
import com.sameer.job.dto.ai.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationAiService {

    private static final String SYSTEM_PROMPT = """
            You are a senior technical recruiter and career coach with 15+ years of
            experience in the Indian tech industry.

            You specialize in candidate evaluation, cover letter writing, skills gap
            analysis, and career development.

            Always provide objective, fair, and actionable assessments based only on
            the information provided.

            When asked for JSON, respond ONLY with valid JSON — no explanation,
            no markdown fences.
            """;

    private final GeminiClient geminiClient;

    public AiTextResponse generateCoverLetter(CoverLetterRequest request) throws Exception {

        String skills = request.getCandidateSkills() != null
                ? String.join(", ", request.getCandidateSkills())
                : "Not Provided";

        String experience = request.getCandidateExperience() != null
                ? String.join(", ", request.getCandidateExperience())
                : "Not Provided";

        String prompt = """
                Write a compelling, personalized cover letter.

                Position: %s
                Job Description: %s
                Target Company: %s

                Candidate Profile:
                - Name: %s
                - Professional Summary: %s
                - Key Skills: %s
                - Relevant Experience: %s

                Candidate Instructions: %s

                Write a 3-paragraph cover letter:

                Paragraph 1 (Opening):
                Express specific enthusiasm for this exact role and company.
                Mention 1 specific thing about the role that excites you.

                Paragraph 2 (Body):
                Connect 2-3 of the candidate's strongest experiences or skills
                directly to the job requirements. Be specific with examples.

                Paragraph 3 (Closing):
                Provide a confident call to action. Express eagerness to discuss
                the opportunity further.

                Rules:
                - Write as the candidate (first person).
                - Be specific and avoid generic statements.
                - Maximum 300 words.
                - Use a professional but warm tone.
                - Do NOT use placeholders like [Company Name].
                - Use the actual company name or say "your team".
                - Do NOT include a subject line or date.
                - Follow Candidate Instructions when provided, unless they conflict with the rules above.
                """.formatted(
                request.getJobTitle(),
                request.getJobDescription() != null
                        ? request.getJobDescription()
                        : "Not provided",
                request.getTargetCompanyName() != null
                        ? request.getTargetCompanyName()
                        : "your organization",
                request.getCandidateName(),
                request.getCandidateSummary() != null
                        ? request.getCandidateSummary()
                        : "Experienced Professional",
                skills,
                experience,
                request.getAdditionalContext() != null && !request.getAdditionalContext().isBlank()
                        ? request.getAdditionalContext()
                        : "None"
        );

        return AiTextResponse.builder()
                             .content(geminiClient.generateText(SYSTEM_PROMPT, prompt))
                             .build();
    }

    public ScreeningScoreResponse scoreCandidate(
            ScreeningScoreRequest req
    ) throws Exception {

        String requiredSkills = req.getRequiredSkills() != null
                ? String.join(", ", req.getRequiredSkills())
                : "Not Provided";

        String candidateSkills = req.getCandidateSkills() != null
                ? String.join(", ", req.getCandidateSkills())
                : "Not Provided";

        String candidateExperience = req.getCandidateExperience() != null
                ? String.join(", ", req.getCandidateExperience())
                : "Not Provided";

        String prompt = """
                Score this job application based on how well the candidate
                matches the job requirements.

                Job Requirements:
                - Title: %s
                - Experience Level Required: %s
                - Required Skills: %s
                - Key Responsibilities: %s

                Candidate Profile:
                - Professional Summary: %s
                - Skills: %s
                - Experience History: %s

                Return JSON using exactly this structure:

                {
                  "score": 85,
                  "skillsMatchScore": 90,
                  "experienceMatchScore": 80,
                  "educationMatchScore": 75,
                  "matchedSkills": ["skill1", "skill2"],
                  "missingSkills": ["skill3"],
                  "strengths": ["strength1", "strength2"],
                  "concerns": ["concern1"],
                  "summary": "2-3 sentence honest assessment of this candidate's fit"
                }

                Scoring:
                - score: Overall match score from 0-100.
                - skillsMatchScore: How well candidate skills match required skills.
                - experienceMatchScore: How well candidate experience matches the
                  required experience level.
                - educationMatchScore: How well candidate education/background fits
                  the role.

                Be objective and fair.
                Do not invent skills, experience, education, or qualifications.
                """.formatted(
                req.getJobTitle() != null
                        ? req.getJobTitle()
                        : "Not Provided",
                req.getExperienceLevel() != null
                        ? req.getExperienceLevel()
                        : "Not Provided",
                requiredSkills,
                req.getResponsibilities() != null
                        ? req.getResponsibilities()
                        : "Not provided",
                req.getCandidateSummary() != null
                        ? req.getCandidateSummary()
                        : "Not provided",
                candidateSkills,
                candidateExperience
        );

        return geminiClient.generateJson(
                SYSTEM_PROMPT,
                prompt,
                ScreeningScoreResponse.class
        );
    }

    public SkillsGapResponse analyzeSkillsGap(
            SkillsGapRequest req
    ) throws Exception {

        String candidateSkills = req.getCandidateSkills() != null
                ? String.join(", ", req.getCandidateSkills())
                : "Not Provided";

        String requiredSkills = req.getRequiredSkills() != null
                ? String.join(", ", req.getRequiredSkills())
                : "Not Provided";

        String prompt = """
                Analyze the skills gap between a candidate and a job requirement.

                Job Title: %s

                Candidate's Current Skills:
                %s

                Skills Required for the Job:
                %s

                Return JSON using exactly this structure:

                {
                  "matchedSkills": [
                    "skills candidate has that are required"
                  ],
                  "missingSkills": [
                    "required skills candidate completely lacks"
                  ],
                  "partialMatch": [
                    "skills candidate has partially or related version of"
                  ],
                  "prioritySkillsToLearn": [
                    "top 3 skills to learn first, in order of importance"
                  ],
                  "learningRecommendations": [
                    {
                      "skill": "skill name",
                      "why": "why this skill is important for the role",
                      "howToLearn": "specific learning resource or approach"
                    }
                  ],
                  "overallReadiness": "Ready or Partially Ready or Needs Development",
                  "summary": "2-sentence honest assessment"
                }

                Rules:
                - Be specific and actionable.
                - Do not invent skills or qualifications.
                - Prioritize skills based on their relevance to the target role.
                """.formatted(
                req.getJobTitle(),
                candidateSkills,
                requiredSkills
        );

        return geminiClient.generateJson(
                SYSTEM_PROMPT,
                prompt,
                SkillsGapResponse.class
        );
    }
}