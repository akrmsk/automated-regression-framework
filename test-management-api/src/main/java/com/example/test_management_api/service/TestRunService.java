package com.example.test_management_api.service;


import com.example.test_management_api.dtos.CreateTestRunRequestDto;
import com.example.test_management_api.dtos.TestRunUpdateDto;
import com.example.test_management_api.model.TestRun;
import com.example.test_management_api.model.enums.TestRunStatus;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestRunService {
//    TestRun saveTestRun(TestRun testRun);
    TestRun createTestRun(CreateTestRunRequestDto requestDto);
    Optional<TestRun> findTestRun(UUID id);
    TestRun updateTestRunStatus(UUID id, TestRunUpdateDto updateDto);
    List<TestRun> getAllTestsByCriteria(@RequestParam(required = false) TestRunStatus status, @RequestParam(required = false) String environment);
}
