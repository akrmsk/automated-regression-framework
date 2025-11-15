package com.example.test_management_api.model;

import com.example.test_management_api.model.enums.TestRunStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TestRun {
    @Id
    @JsonProperty("runId")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TestRunStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String reportUrl;

    private String environment;

    private String tags;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String errorDetails;

    @JsonProperty("failedTestCount")
    private Integer failedTestCount;

    private String screenshotPath;
}
