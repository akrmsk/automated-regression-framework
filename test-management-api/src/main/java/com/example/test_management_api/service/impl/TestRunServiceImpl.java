package com.example.test_management_api.service.impl;


import com.example.test_management_api.model.TestRun;
import com.example.test_management_api.repository.TestRunRepository;
import com.example.test_management_api.service.TestRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestRunServiceImpl implements TestRunService {
    private final TestRunRepository testRunRepository;

    public TestRun saveTestRun(TestRun testRun){
        return testRunRepository.save(testRun);
    }

    public Optional<TestRun> findTestRun(UUID id){
        return testRunRepository.findById(id);
    }

    @Override
    public List<TestRun> getAllTests() {
        List<TestRun> testRuns=testRunRepository.findAll();
        return testRuns;
    }

}
