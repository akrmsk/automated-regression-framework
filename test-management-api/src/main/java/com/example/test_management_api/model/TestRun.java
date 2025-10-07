package com.example.test_management_api.model;


import com.example.test_management_api.model.enums.TestRunStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TestRunStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String reportUrl;
}
