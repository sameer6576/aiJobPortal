package com.sameer.job.payload;

import com.sameer.job.domain.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationStatus {
    private ApplicationStatus status;
}
