package com.example.test_management_api.service;


import com.example.test_management_api.model.TestRun;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestRunService {
    TestRun saveTestRun(TestRun testRun);
    Optional<TestRun> findTestRun(UUID id);

    List<TestRun> getAllTests();
}
