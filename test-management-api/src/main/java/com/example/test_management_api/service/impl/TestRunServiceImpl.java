package com.example.test_management_api.service.impl;


import com.example.test_management_api.dtos.CreateTestRunRequestDto;
import com.example.test_management_api.dtos.TestRunUpdateDto;
import com.example.test_management_api.model.TestRun;
import com.example.test_management_api.model.enums.TestRunStatus;
import com.example.test_management_api.repository.TestRunRepository;
import com.example.test_management_api.service.TestRunService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestRunServiceImpl implements TestRunService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunServiceImpl.class);
    private final TestRunRepository testRunRepository;
//
//    public TestRun saveTestRun(TestRun testRun){
//        return testRunRepository.save(testRun);
//    }

    @Override
    public TestRun createTestRun(CreateTestRunRequestDto requestDto) {
        TestRun testRun=new TestRun();

        testRun.setId(UUID.randomUUID());
        testRun.setStatus(TestRunStatus.SCHEDULED);
        testRun.setStartTime(LocalDateTime.now());
        testRun.setEnvironment(requestDto.getEnvironment());
        testRun.setTags(requestDto.getTags());
        LOGGER.info("Creating new TestRun for environment: {}", requestDto.getEnvironment());
        return testRunRepository.save(testRun);
    }

    public Optional<TestRun> findTestRun(UUID id){
        return testRunRepository.findById(id);
    }

    public TestRun updateTestRunStatus(UUID id, TestRunUpdateDto updateDto) {

        TestRun existingTestRun = testRunRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test run with ID " + id + " not found to update!"));
        existingTestRun.setStatus(updateDto.getStatus());
        existingTestRun.setEndTime(LocalDateTime.now());
        existingTestRun.setReportUrl(updateDto.getReportUrl());
        //for error messages
        existingTestRun.setErrorMessage(updateDto.getErrorMessage());
        existingTestRun.setErrorDetails(updateDto.getErrorDetails());
        existingTestRun.setFailedTestCount(updateDto.getFailedTestCount());
        existingTestRun.setScreenshotPath(updateDto.getScreenshotPath());

        LOGGER.info("Before saving: FailedTestCount = {}", existingTestRun.getFailedTestCount());

        return testRunRepository.save(existingTestRun);
    }
    @Transactional // Good practice, can add (readOnly = true) for optimization
    @Override
    public List<TestRun> getAllTestsByCriteria(TestRunStatus status, String environment) {
        // Case 1: Both filters are provided
        if(status != null && environment != null){
            LOGGER.info("Fetching TestRuns by status '{}' and environment '{}'", status, environment);
            return testRunRepository.findByStatusAndEnvironment(status, environment);
        }
        // Case 2: Only status is provided
        else if(status != null){ // No need to check environment == null here
            LOGGER.info("Fetching TestRuns by status '{}'", status);
            return testRunRepository.findByStatus(status);
        }
        // Case 3: Only environment is provided
        else if(environment != null){ // No need to check status == null here
            LOGGER.info("Fetching TestRuns by environment '{}'", environment);
            return testRunRepository.findByEnvironment(environment);
        }
        // Case 4: Neither filter is provided
        else{
            LOGGER.info("Fetching all TestRuns (no filters provided)");
            return testRunRepository.findAll();
        }
    }

}
