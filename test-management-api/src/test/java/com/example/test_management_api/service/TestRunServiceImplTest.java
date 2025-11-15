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

    @Mock
    private TestRunRepository testRunRepository;

    @InjectMocks // Creates a real instance of TestRunServiceImpl and injects the mocks (@Mock) into it
    private TestRunServiceImpl testRunService;


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


    @Test
    void whenFindByStatusOnly_shouldCallRepositoryFindByStatus() {
        TestRunStatus filterStatus = TestRunStatus.FAILED;
        String filterEnvironment = null;
        List<TestRun> expectedRuns = List.of(run1_failed_qa);

        given(testRunRepository.findByStatus(filterStatus)).willReturn(expectedRuns);

        List<TestRun> actualRuns = testRunService.getAllTestsByCriteria(filterStatus, filterEnvironment);

        assertNotNull(actualRuns);
        assertEquals(1, actualRuns.size());
        assertEquals(run1_failed_qa.getId(), actualRuns.get(0).getId());
        assertEquals(filterStatus, actualRuns.get(0).getStatus());
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
        TestRunStatus filterStatus = null; // No status filter
        String filterEnvironment = null;   // No environment filter

        List<TestRun> expectedRuns = List.of(run1_failed_qa, run2_completed_qa, run3_completed_staging);

        given(testRunRepository.findAll()).willReturn(expectedRuns);


        List<TestRun> actualRuns = testRunService.getAllTestsByCriteria(filterStatus, filterEnvironment);

        assertNotNull(actualRuns);
        assertEquals(3, actualRuns.size());
        // Check if the IDs match (order doesn't matter)
        List<UUID> actualIds = actualRuns.stream().map(TestRun::getId).collect(Collectors.toList());
        assertTrue(actualIds.contains(run1_failed_qa.getId()));
        assertTrue(actualIds.contains(run2_completed_qa.getId()));
        assertTrue(actualIds.contains(run3_completed_staging.getId()));


        verify(testRunRepository).findAll();
        verify(testRunRepository, never()).findByStatus(any());
        verify(testRunRepository, never()).findByEnvironment(any());
        verify(testRunRepository, never()).findByStatusAndEnvironment(any(), any());
    }

}