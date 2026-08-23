package com.sameer.job.payload;

import lombok.Data;

import java.util.List;

@Data
public class SkillsGapResponse {
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> partialMatch;
    private List<String> prioritySkillsToLearn;
    private List<LearningRecommendation> learningRecommendations;
    private String overallReadiness;
    private String summary;

    public static class LearningRecommendation {
        private String skill;
        private String why;
        private String howToLearn;
    }
}
