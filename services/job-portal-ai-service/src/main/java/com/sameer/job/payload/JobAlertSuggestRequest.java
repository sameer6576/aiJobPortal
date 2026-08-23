package com.sameer.job.payload;


import lombok.Data;

import java.util.List;

@Data
public class JobAlertSuggestRequest {
    private List<String> skills;
    private String experienceLevel;
    private List<String> previousJobTitles;
    private List<String> educations;
}
