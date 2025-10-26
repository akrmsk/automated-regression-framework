package com.example.test_management_api.model;

import com.example.test_management_api.model.enums.TestRunStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class TestRunSerializationTest {

    @Autowired
    private JacksonTester<TestRun> json;

    @Test
    public void testSerializeTestRun() throws Exception {
        // Create a TestRun with all fields set
        TestRun testRun = new TestRun();
        testRun.setId(UUID.randomUUID());
        testRun.setStatus(TestRunStatus.COMPLETED);
        testRun.setStartTime(LocalDateTime.now());
        testRun.setEndTime(LocalDateTime.now());
        testRun.setReportUrl("http://example.com/report");
        testRun.setErrorMessage("Test error message");
        testRun.setErrorDetails("Detailed error information");
        testRun.setFailedTestCount(5);

        // Serialize to JSON
        JsonContent<TestRun> result = json.write(testRun);

        // Check that the failedTestCount field is included in the JSON
        assertThat(result).extractingJsonPathNumberValue("$.failedTestCount").isEqualTo(5);
        
        // Also check other fields to ensure they're serialized correctly
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("COMPLETED");
        assertThat(result).extractingJsonPathStringValue("$.errorMessage").isEqualTo("Test error message");
        assertThat(result).extractingJsonPathStringValue("$.errorDetails").isEqualTo("Detailed error information");
        
        // Print the JSON for debugging
        System.out.println("[DEBUG_LOG] Serialized JSON: " + result.getJson());
    }
}