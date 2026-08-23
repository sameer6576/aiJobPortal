package com.sameer.job.payload;

import lombok.Data;

import java.util.List;

@Data
public class CareerFeedbackResponse {
    private int profileStrength;

    private List<String> shortListingIssues;

    private List<Improvement> improvements;

    private List<JobTarget> targetJobs;

    private String overallSummary;

    @Data
    public static class Improvement{
        private String area;
        private String issue;
        private String action;
        private String priority;
    }

    @Data
    public static class JobTarget {
        private String jobTitle;
        private String reason;
        private String skillMatch;
    }
}
