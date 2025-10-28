package com.example.test_management_api.controller;

import com.example.test_management_api.dtos.CreateTestRunRequestDto;
import com.example.test_management_api.dtos.TestRunUpdateDto;
import com.example.test_management_api.model.TestRun;
import com.example.test_management_api.model.enums.TestRunStatus;
import com.example.test_management_api.service.RabbitMQProducer;
import com.example.test_management_api.service.TestRunService;
import com.example.test_management_api.service.impl.TestRunServiceImpl;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunServiceImpl.class);
    private final TestRunService testRunService;
    private final RabbitMQProducer rabbitMQProducer;

    @GetMapping("/runs/{id}")
    public ResponseEntity<TestRun> getTestRunById(@PathVariable UUID id){
        Optional<TestRun> testRunOptional= testRunService.findTestRun(id);
        if(testRunOptional.isPresent()){
            return ResponseEntity.ok(testRunOptional.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/runs")
    public ResponseEntity<TestRun> createTestRun(@RequestBody CreateTestRunRequestDto testRunRequestDto){
        TestRun testRun=new TestRun();
        testRun.setId(UUID.randomUUID());
        testRun.setStatus(TestRunStatus.SCHEDULED);
        testRun.setStartTime(LocalDateTime.now());
        testRun.setEnvironment(testRunRequestDto.getEnvironment());
        TestRun savedTestRun= testRunService.createTestRun(testRunRequestDto);
        rabbitMQProducer.sendTestRunJob(savedTestRun);

        return new ResponseEntity<>(savedTestRun,HttpStatus.CREATED);
    }

    @GetMapping("/runs")
    public ResponseEntity<List<TestRun>> getAllTestRuns(@RequestParam(required = false) TestRunStatus status,@RequestParam(required = false) String environment){
        List<TestRun> TestRuns=testRunService.getAllTestsByCriteria(status,environment);
        return new ResponseEntity<>(TestRuns,HttpStatus.OK);
    }

    @PutMapping("/runs/{id}")
    public ResponseEntity<TestRun> updateTestRun(@PathVariable UUID id,@RequestBody TestRunUpdateDto testRunUpdateDto){
        TestRun updatedTestRun=testRunService.updateTestRunStatus(id,testRunUpdateDto);
        return ResponseEntity.ok(updatedTestRun);
    }
}
