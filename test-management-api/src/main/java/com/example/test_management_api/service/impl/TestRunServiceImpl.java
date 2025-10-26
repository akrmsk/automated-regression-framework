package com.example.test_management_api.service.impl;


import com.example.test_management_api.dtos.TestRunUpdateDto;
import com.example.test_management_api.model.TestRun;
import com.example.test_management_api.repository.TestRunRepository;
import com.example.test_management_api.service.TestRunService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestRunServiceImpl implements TestRunService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunServiceImpl.class);
    private final TestRunRepository testRunRepository;

    public TestRun saveTestRun(TestRun testRun){
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

        LOGGER.info("Before saving: FailedTestCount = {}", existingTestRun.getFailedTestCount());

        return testRunRepository.save(existingTestRun);
    }

    @Override
    public List<TestRun> getAllTests() {
        List<TestRun> testRuns=testRunRepository.findAll();
        return testRuns;
    }

}
