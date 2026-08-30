package com.sameer.job.service;

import com.sameer.job.client.GeminiClient;
import com.sameer.job.dto.ai.AiTextResponse;
import com.sameer.job.payload.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final GeminiClient geminiClient;

    String SYSTEM_PROMPT = """
            You are a senior resume writer and career coach with 15+ years of experience in the Indian technology job market.
            
            You specialize in:
            
            * ATS-optimized resumes
            * Career coaching
            * Professional branding
            * Resume positioning for software engineering and technology roles
            
            Your goal is to create concise, compelling, and results-oriented professional content that improves a candidate’s chances of passing ATS screening and getting shortlisted.
            
            ### Guidelines
            
            * Always be specific, relevant, and results-oriented.
            * Prioritize measurable achievements, technical impact, and business outcomes.
            * Use industry-standard ATS keywords naturally without keyword stuffing.
            * Tailor content to the target job title and job description when provided.
            * Never use generic or overused phrases such as "hard-working", "team player", "passionate", or similar clichés.
            * Never invent skills, experience, achievements, metrics, or qualifications.
            * Use clear, professional, and concise language.
            * Prioritize recent and relevant experience.
            * Maintain a professional tone appropriate for the Indian and global technology job market.
            
            ### JSON Output Rule
            
            When the user explicitly requests JSON, respond with **ONLY valid JSON**.
            
            Do not include:
            
            * Markdown code fences
            * Explanations outside the JSON
            * Comments
            * Introductory or concluding text
            
            Ensure the JSON is properly formatted, syntactically valid, and directly usable by a software application.
            
            """;


    public AiTextResponse generateProfessionalSummary(ResumeSummaryRequest req) throws Exception {
        String experiences = req.getWorkExperiences() != null ?
                req.getWorkExperiences().stream().map(
                        e -> e.getJobTitle() + "at" + e.getCompanyName() +
                             (e.getDescription() != null && !e.getDescription()
                                                              .isBlank() ? ": " + e.getDescription() : "")
                ).collect(Collectors.joining("; ")) : "Not provided";

        String skills = req.getSkills() != null ? String.join(", ", req.getSkills()) : "Not provided";
        String educations = req.getEducations() != null
                ? req.getEducations().stream()
                     .map(e -> e.getDegree() + (e.getFieldOfStudy() != null ? " in " + e.getInstitutionName() : ""))
                     .collect(Collectors.joining("; ")) : "Not provided";

        String prompt = """
                Write a compelling, professional resume summary based on the following candidate profile:
                
                **Target Job Title:** %s
                **Years of Experience:** %d
                **Work Experience:** %s
                **Key Skills:** %s
                **Education:** %s
                
                Write a **3–4 sentence professional summary** that:
                
                1. Opens with the candidate’s seniority level and area of expertise.
                2. Highlights 2–3 key achievements, strengths, or measurable impacts.
                3. Mentions specific technical skills relevant to the target job.
                4. Ends with a strong value proposition aligned with the candidate’s career goal.
                
                **Rules:**
                
                * Write in **third person**; do not start with "I".
                * Be specific, concise, and results-oriented.
                * Prioritize measurable achievements when available.
                * Use ATS-friendly keywords naturally.
                * Avoid generic buzzwords and unnecessary adjectives.
                * Do not invent experience, skills, achievements, or metrics.
                * Keep the summary **under 80 words**.
                * Return **only the professional summary**, with no heading or additional explanation.
                """.formatted(
                req.getTargetJobTitle() != null ? req.getTargetJobTitle() : "Software Developer",
                req.getYearsOfExperience() != null ? req.getYearsOfExperience() : 0,
                experiences, skills, educations
        );

        return AiTextResponse.builder()
                             .content(geminiClient.generateText(SYSTEM_PROMPT, prompt))
                             .build();
    }

    public WorkExperienceBulletsResponse generateWorkExperienceBullets(WorkExperienceBulletRequest req) throws Exception {

        String prompt = """
                Transform the candidate’s work experience into powerful, ATS-friendly resume bullet points.
                
                Role: %s at %s
                Raw Description: %s
                Achievements/Hints: %s
                
                Generate exactly 4–5 bullet points that:
                
                1. Start with strong action verbs such as Developed, Led, Implemented, Architected,
                   Optimized, Reduced, Increased, Delivered, or Built.
                2. Include quantifiable metrics such as percentages, numbers, time saved,
                   performance improvements, or scale whenever available.
                3. Highlight both technical achievements and measurable business impact.
                4. Keep each bullet concise and under 20 words.
                5. Naturally incorporate relevant ATS keywords, technologies, frameworks,
                   methodologies, and domain-specific terminology.
                6. Focus on outcomes rather than simply listing responsibilities.
                7. Do not invent metrics, technologies, achievements, or responsibilities
                   not supported by the provided information.
                8. Use past tense for previous roles and present tense for current roles.
                9. Avoid generic phrases such as "responsible for", "worked on", "helped",
                   "hard-working", or "team player".
                
                Output Format:
                
                When generating the response, return ONLY valid JSON in exactly this structure:
                
                {
                  "bullets": [
                    "bullet point 1",
                    "bullet point 2",
                    "bullet point 3",
                    "bullet point 4",
                    "bullet point 5"
                  ]
                }
                
                Do not include markdown fences, explanations, headings, or any text outside the JSON.
                """.formatted(
                req.getJobTitle(),
                req.getCompany() != null ? req.getCompany() : "The company",
                req.getRawDescription(),
                req.getAchievementsHint() != null ? req.getAchievementsHint() : "None"
        );
        return geminiClient.generateJson(SYSTEM_PROMPT, prompt, WorkExperienceBulletsResponse.class);
    }

    public CareerFeedbackResponse generateCareerFeedback(CareerFeedbackRequest req) throws Exception {

        String prompt = """
                Analyze the provided resume and deliver an honest, actionable career feedback report tailored to the candidate’s target role.
                
                **Target Job Title (if provided):** %s
                
                **Resume Content:**
                %s
                
                ### Your Role
                
                Act as a senior resume writer, recruiter, and career coach with 15+ years of experience in the Indian technology job market.
                
                Evaluate the resume from both an **ATS and recruiter perspective**, focusing on:
                
                * Job-market readiness
                * ATS compatibility
                * Technical positioning
                * Achievement strength
                * Relevance to the target role
                * Recruiter shortlisting potential
                * Career progression and positioning
                
                ### Output Requirements
                
                Return **ONLY valid JSON** using exactly this structure:
                
                {
                "profileStrength": 65,
                "shortlistingIssues": [
                "Reason 1 why recruiters may skip this profile",
                "Reason 2",
                "Reason 3"
                ],
                "improvements": [
                {
                "area": "Skills | Summary | Experience | Education | Projects | General",
                "issue": "Specific problem identified in the resume",
                "recommendation": "Specific, actionable improvement",
                "priority": "High | Medium | Low"
                }
                ],
                "targetJobs": [
                {
                "jobTitle": "Recommended Job Title",
                "reason": "Why this role realistically suits the candidate's current skills and experience"
                }
                ],
                "overallSummary": "2-3 sentences of honest, encouraging career advice"
                }
                
                ### Rules
                
                1. **profileStrength:** Return an integer from 0–100 representing the candidate’s overall job-market readiness for the target role.
                2. **shortlistingIssues:** Provide exactly **3–5 candid reasons** recruiters may reject or skip the resume.
                3. **improvements:** Provide exactly **4–6 improvements**, ordered from highest to lowest priority.
                4. Each improvement must identify the specific resume section and explain exactly what should change.
                5. **targetJobs:** Recommend exactly **3–5 realistic job titles** based on the candidate’s actual skills and experience level.
                6. Do not recommend roles that require significantly more experience or skills than demonstrated.
                7. Mention **actual technologies, tools, frameworks, projects, achievements, and resume sections by name** whenever relevant.
                8. Prioritize measurable achievements and technical impact over generic responsibilities.
                9. Identify missing or weak ATS keywords when relevant to the target role.
                10. Be candid about weaknesses, but make every criticism constructive and actionable.
                11. Do **not** invent experience, technologies, metrics, certifications, achievements, or qualifications.
                12. Do not give generic advice such as "improve your resume" without explaining exactly how.
                13. Evaluate the resume against realistic expectations for the **Indian technology job market**.
                14. If no target job title is provided, infer suitable roles from the candidate’s experience and skills.
                15. Keep `overallSummary` concise, honest, encouraging, and actionable.
                16. Return **ONLY valid JSON**. Do not use markdown fences, explanations, headings, or text outside the JSON.
                """.formatted(
                req.getTargetJobTitle() != null ? req.getTargetJobTitle() : "Not specified",
                req.getResumeContent()
        );

        return geminiClient.generateJson(SYSTEM_PROMPT, prompt, CareerFeedbackResponse.class);

    }

    public ResumeImprovementResponse getResumeImprovementTips(ResumeImprovementRequest req) throws Exception {
        String prompt = """
                Analyze the provided resume and deliver a concise, objective, and actionable improvement report tailored to the target job.
                
                **Target Job Title:** %s
                
                **Resume Content:**
                %s
                
                ### Your Role
                
                Act as a senior resume writer, recruiter, and career coach with 15+ years of experience in the Indian technology job market. Evaluate the resume from both an **ATS and recruiter perspective**.
                
                ### Analyze
                
                Evaluate:
                
                * ATS keyword alignment with the target role
                * Professional summary and positioning
                * Work experience and measurable achievements
                * Technical skills and relevance
                * Projects and technical depth
                * Education and certifications
                * Resume structure, clarity, and impact
                * Overall competitiveness for the target role
                
                ### Output Format
                
                Return **ONLY valid JSON** in exactly this structure:
                
                {
                "overallScore": 72,
                "strengths": [
                "Specific strength supported by the resume",
                "Specific strength supported by the resume",
                "Specific strength supported by the resume"
                ],
                "improvements": [
                {
                "section": "Summary | Experience | Skills | Education | Projects | General",
                "issue": "Specific problem identified",
                "recommendation": "Specific action the candidate should take",
                "priority": "High | Medium | Low"
                }
                ],
                "summary": "Two concise sentences providing an honest overall assessment and the most important next step."
                }
                
                ### Rules
                
                1. `overallScore` must be an integer from **0–100**, representing overall job-market readiness for the target role.
                2. Provide exactly **4–6 improvements**, ordered by priority.
                3. Provide **3–5 specific strengths** based only on information present in the resume.
                4. Every improvement must be specific and actionable; avoid generic advice.
                5. Mention actual technologies, tools, frameworks, projects, achievements, or sections by name whenever relevant.
                6. Identify missing, weak, outdated, or poorly positioned ATS keywords when applicable.
                7. Prioritize improvements that would have the greatest impact on recruiter shortlisting.
                8. Highlight missing metrics or opportunities to quantify impact where appropriate.
                9. Do not invent experience, skills, achievements, metrics, certifications, or qualifications.
                10. Be honest and critical, but constructive and solution-oriented.
                11. Tailor recommendations specifically to the **Target Job Title** rather than giving generic resume advice.
                12. If the target job title is missing or unclear, infer the most suitable role from the resume.
                13. Do not recommend changes that would misrepresent the candidate's actual experience.
                14. Return **ONLY valid JSON** with no markdown, code fences, explanations, or additional text.
                """.formatted(
                req.getTargetJobTitle() != null ? req.getTargetJobTitle() : "Not Specified",
                req.getResumeContent()
        );

        return geminiClient.generateJson(SYSTEM_PROMPT, prompt, ResumeImprovementResponse.class);
    }

}

