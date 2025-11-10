package com.example.test_runner_worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class TestRunnerWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestRunnerWorkerApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean("testExecutor")
    public TaskExecutor testExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // The number of concurrent tests will be limited by this pool
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("TestExecutor-");
        executor.initialize();
        return executor;
    }
}
