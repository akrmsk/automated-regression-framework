package com.example.test_management_api.service;
import com.example.test_management_api.service.impl.TestRunServiceImpl;
import com.example.test_management_api.model.TestRun;
import com.example.test_management_api.model.enums.TestRunStatus;
import com.example.test_management_api.repository.TestRunRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given; // BDDMockito style for given/when/then
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class) // Tells JUnit 5 to use Mockito
class TestRunServiceImplTest {

    @Mock // Creates a mock instance of TestRunRepository
    private TestRunRepository testRunRepository;

    @InjectMocks // Creates a real instance of TestRunServiceImpl and injects the mocks (@Mock) into it
    private TestRunServiceImpl testRunService;

    // Inside TestRunServiceImplTest class...

    private TestRun run1_failed_qa;
    private TestRun run2_completed_qa;
    private TestRun run3_completed_staging;

    @BeforeEach
    void setUp() {
        // Create some sample TestRun objects for different scenarios
        run1_failed_qa = new TestRun();
        run1_failed_qa.setId(UUID.randomUUID());
        run1_failed_qa.setStatus(TestRunStatus.FAILED);
        run1_failed_qa.setEnvironment("QA");

        run2_completed_qa = new TestRun();
        run2_completed_qa.setId(UUID.randomUUID());
        run2_completed_qa.setStatus(TestRunStatus.COMPLETED);
        run2_completed_qa.setEnvironment("QA");

        run3_completed_staging = new TestRun();
        run3_completed_staging.setId(UUID.randomUUID());
        run3_completed_staging.setStatus(TestRunStatus.COMPLETED);
        run3_completed_staging.setEnvironment("Staging");
    }
    // Inside TestRunServiceImplTest class...


    @Test
    void whenFindByStatusOnly_shouldCallRepositoryFindByStatus() {
        // --- Arrange ---
        TestRunStatus filterStatus = TestRunStatus.FAILED;
        String filterEnvironment = null; // Environment is not provided for this test
        List<TestRun> expectedRuns = List.of(run1_failed_qa); // Expect only the failed run

        // Tell the mock repository: "When findByStatus(FAILED) is called, return our expected list"
        given(testRunRepository.findByStatus(filterStatus)).willReturn(expectedRuns);

        // --- Act ---
        // Call the service method we are testing
        List<TestRun> actualRuns = testRunService.getAllTestsByCriteria(filterStatus, filterEnvironment);

        // --- Assert ---
        // Verify the results
        assertNotNull(actualRuns); // Make sure the list is not null
        assertEquals(1, actualRuns.size()); // Check if it has the expected number of items
        assertEquals(run1_failed_qa.getId(), actualRuns.get(0).getId()); // Check if it contains the correct run
        assertEquals(filterStatus, actualRuns.get(0).getStatus()); // Double-check the status
    }

    @Test
    void whenFindByEnvironmentOnly_shouldCallRepositoryFindByEnvironment(){
        TestRunStatus status=null;
        String environment="QA";
        List<TestRun> expectedRuns=List.of(run1_failed_qa,run2_completed_qa);
        given(testRunRepository.findByEnvironment(environment)).willReturn(expectedRuns);
        List<TestRun> actualRuns=testRunService.getAllTestsByCriteria(status,environment);
        assertNotNull(actualRuns);
        assertEquals(2,actualRuns.size());
        assertEquals(run1_failed_qa.getId(),actualRuns.get(0).getId());
        assertEquals(run2_completed_qa.getId(),actualRuns.get(1).getId());
    }

    @Test
    void whenFindByStatusAndEnvironment_shouldCallRepositoryFindByStatusAndEnvironment(){
        //step 1
        TestRunStatus status = TestRunStatus.COMPLETED;
        String environment="QA";
        List<TestRun> expectedRuns=List.of(run2_completed_qa);

        given(testRunRepository.findByStatusAndEnvironment(status,environment)).willReturn(expectedRuns);
        //step 2

        List<TestRun> actualRuns = testRunService.getAllTestsByCriteria(TestRunStatus.COMPLETED, "QA");

        //step 3
        assertNotNull(actualRuns);
        assertEquals(1, actualRuns.size());
        assertEquals(run2_completed_qa.getId(), actualRuns.get(0).getId());
        verify(testRunRepository).findByStatusAndEnvironment(TestRunStatus.COMPLETED, "QA"); // Check this WAS called
        verify(testRunRepository, never()).findByStatus(any());
        verify(testRunRepository, never()).findByEnvironment(any());
        verify(testRunRepository, never()).findAll();
    }


    @Test
    void whenNoFilters_shouldCallRepositoryFindAll() { // Renamed for clarity
        // --- Arrange ---
        TestRunStatus filterStatus = null; // No status filter
        String filterEnvironment = null;   // No environment filter

        // Expected result: A list containing ALL sample runs
        List<TestRun> expectedRuns = List.of(run1_failed_qa, run2_completed_qa, run3_completed_staging);

        // Program the mock: "When findAll() is called, return our list of all sample runs"
        given(testRunRepository.findAll()).willReturn(expectedRuns);

        // --- Act ---
        // Call the service method with null filters
        List<TestRun> actualRuns = testRunService.getAllTestsByCriteria(filterStatus, filterEnvironment);

        // --- Assert ---
        // Verify the results
        assertNotNull(actualRuns);         // Should not be null
        assertEquals(3, actualRuns.size()); // Should contain all 3 sample runs
        // Check if the IDs match (order doesn't matter)
        List<UUID> actualIds = actualRuns.stream().map(TestRun::getId).collect(Collectors.toList());
        assertTrue(actualIds.contains(run1_failed_qa.getId()));
        assertTrue(actualIds.contains(run2_completed_qa.getId()));
        assertTrue(actualIds.contains(run3_completed_staging.getId()));


        // Verify that ONLY findAll() was called on the repository
        verify(testRunRepository).findAll(); // Check this WAS called
        verify(testRunRepository, never()).findByStatus(any()); // Check others were NOT called
        verify(testRunRepository, never()).findByEnvironment(any());
        verify(testRunRepository, never()).findByStatusAndEnvironment(any(), any());
    }

}