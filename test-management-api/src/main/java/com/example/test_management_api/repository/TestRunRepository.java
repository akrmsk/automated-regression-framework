package com.example.test_management_api.repository;

import com.example.test_management_api.model.TestRun;
import com.example.test_management_api.model.enums.TestRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TestRunRepository extends JpaRepository<TestRun, UUID> {
    List<TestRun> findByStatusAndEnvironment(TestRunStatus status, String environment);

    List<TestRun> findByStatus(TestRunStatus status);

    List<TestRun> findByEnvironment(String environment);
}
