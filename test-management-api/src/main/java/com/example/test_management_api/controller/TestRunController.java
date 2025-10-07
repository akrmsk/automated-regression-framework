package com.example.test_management_api.controller;

import com.example.test_management_api.model.TestRun;
import com.example.test_management_api.model.enums.TestRunStatus;
import com.example.test_management_api.service.TestRunService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class TestRunController {

    private final TestRunService testRunService;

    @GetMapping("/runs/{id}")
    public ResponseEntity<TestRun> getTestRun(@PathVariable UUID id){
        Optional<TestRun> testRunOptional= testRunService.findTestRun(id);
        if(testRunOptional.isPresent()){
            return ResponseEntity.ok(testRunOptional.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/runs")
    public ResponseEntity<TestRun> createTestRun(){
        TestRun testRun=new TestRun();
        testRun.setId(UUID.randomUUID());
        testRun.setStatus(TestRunStatus.SCHEDULED);
        testRun.setStartTime(LocalDateTime.now());
        testRun.setEndTime(LocalDateTime.now());
        TestRun savedTestRun= testRunService.saveTestRun(testRun);
        return new ResponseEntity<>(savedTestRun,HttpStatus.CREATED);
    }

    @GetMapping("/runs")
    public ResponseEntity<List<TestRun>> getAllTestRuns(){
        List<TestRun> TestRuns=testRunService.getAllTests();
        return new ResponseEntity<>(TestRuns,HttpStatus.OK);
    }
}
