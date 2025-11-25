Getting Started

This project is fully containerized with Docker. To run the entire framework, follow these steps.

Prerequisites

Docker

Docker Compose

1. Create the Environment File

This project uses an .env file to manage secret passwords.
Create a file named .env in the root of the project with the following content:
```env
# RabbitMQ Credentials
MQ_USER=user
MQ_PASSWORD=password

# MySQL Credentials
DB_USER=api_user
DB_PASSWORD=apipassword
DB_ROOT_PASSWORD=rootpassword
```


2. Build and Run the Application

Build all services:
```bash
docker-compose build
```


Start all services in the background:
```bash
docker-compose up -d
```

3. Run Your First Test

Trigger a test run by sending a POST request to the API.

4. View the Results

Open the Test Automation Dashboard in your browser to see the results in real-time:
'''
http://localhost:8081
'''
# Automated Regression Test Suite Framework

## 🛠️ Tech Stack

### **Core Framework & Language**
* **Java 21**: Core programming language.
* **Spring Boot 3.2.5**: Application framework for the API and Worker modules.
* **Maven**: Dependency management and build tool.

### **Testing Libraries**
* **Selenium WebDriver**: For Web UI automation (Browser testing).
* **REST Assured**: For API validation and testing.
* **JUnit 5**: Testing framework for unit and integration tests.

### **Messaging & Database**
* **RabbitMQ**: Message broker for asynchronous job processing and decoupling API from Worker.
* **MySQL 8.0**: Relational database for persisting test runs and results.
* **Spring Data JPA (Hibernate)**: ORM for database interactions.

### **Frontend (Dashboard)**
* **HTML5 / CSS3**: Structure and styling for the dashboard.
* **Vanilla JavaScript**: Logic for fetching and displaying real-time run data.
* **Nginx**: Web server for hosting the static dashboard files.

### **DevOps & Infrastructure**
* **Docker**: Containerization of all services (API, Worker, DB, Queue, Dashboard).
* **Docker Compose**: Orchestration for running the multi-container environment locally.
* **Selenium Grid (Standalone Chrome)**: For executing browser tests in a containerized environment.

### **Reporting**
* **Thymeleaf**: Template engine for generating custom HTML test reports.
* **Allure Report**: (Integrated) Advanced reporting with detailed logs and trend analysis.

## 1. Project Overview
**Project Name:** Automated Regression Test Suite Framework

**Description:**
A framework to automate regression tests for web and API applications using core Java. Key features:

*   Selenium WebDriver for web UI tests.
*   REST-Assured for API testing.
*   Parallel execution using multithreading.
*   Spring Boot RESTful APIs for scheduling, execution control, and querying results.
*   Reports in HTML, CSV, JUnit formats with failure screenshots and logs.

**Objective:**
Ensure reliable software releases, reduce manual oversight, and improve regression test efficiency.

## 2. Modules / Components
| Module              | Description                                | Key Features                                             |
| ------------------- | ------------------------------------------ | -------------------------------------------------------- |
| Test Management API | RESTful API for managing test runs         | Schedule tests, query results, manage test metadata      |
| Test Runner Worker  | Executes tests from RabbitMQ queue         | Selenium/REST-Assured execution, parallel execution, retries |
| Reporting Module    | Generates test reports                     | HTML/CSV/JUnit formats, failure screenshots, logs        |
| Messaging Layer     | RabbitMQ queue handling                    | Job enqueue/dequeue, fair dispatch, retry logic          |
| Database Module     | Stores run results                         | JDBC/MySQL/H2 integration, historical data tracking      |
| Dashboard (Optional)| Visualizes test runs                       | Web UI showing status, run history, reports              |

## 3. Key Classes & Responsibilities
| Class             | Module    | Responsibility                                           |
| ----------------- | --------- | -------------------------------------------------------- |
| TestRunController   | API       | Exposes REST endpoints to start/stop/query test runs     |
| TestRunService    | API       | Business logic for managing test execution               |
| TestRunRepository | API       | CRUD operations for test run records                     |
| WorkerListener    | Worker    | Listens to RabbitMQ queue and triggers test execution    |
| TestExecutor      | Worker    | Runs Selenium/REST-Assured tests, handles retries        |
| ReportGenerator   | Reporting | Generates HTML, CSV, and JUnit reports                   |
| DatabaseManager   | DB Module | Inserts, updates, queries test run data                  |
| DockerCompose     | Infra     | Orchestrates API, Worker, RabbitMQ containers            |

## 4. Product Backlog
| ID  | Feature/User Story                  | Priority | Acceptance Criteria                                      |
| --- | ----------------------------------- | -------- | -------------------------------------------------------- |
| PB1 | API to schedule test runs           | High     | POST /api/runs creates a new run                         |
| PB2 | Worker executes Selenium/REST-Assured tests | High     | Worker consumes job, executes tests, generates result    |
| PB3 | Parallel execution                  | High     | Multiple tests run concurrently without conflicts        |
| PB4 | Reporting                           | High     | HTML/CSV/JUnit reports created per run, screenshots captured |
| PB5 | Store results in DB                 | Medium   | Test runs persisted with status and report links         |
| PB6 | Dashboard UI                        | Medium   | Displays run history and reports                         |
| PB7 | Retry mechanism                     | Medium   | Failed tests retried automatically                       |
| PB8 | Tag-based execution                 | Medium   | Only selected test categories execute                    |
| PB9 | Docker Compose setup                | Medium   | All services run together locally via Docker             |

## 5. Sprint-wise Tasks
| Sprint   | Focus Area                  | Tasks / Deliverables                                     |
| -------- | --------------------------- | -------------------------------------------------------- |
| Sprint 1 | Foundation Setup            | Initialize Spring Boot projects, Docker RabbitMQ, Worker skeleton, API skeleton |
| Sprint 2 | Messaging Backbone          | RabbitMQ queues, publisher & consumer integration, logging messages |
| Sprint 3 | Test Execution              | Selenium & REST-Assured execution, raw logs, parallel execution |
| Sprint 4 | Reporting Layer             | HTML/CSV/JUnit reports, failure screenshots, API result updates |
| Sprint 5 | Docker Compose & Mock API   | Local integration of API + Worker + RabbitMQ, end-to-end testing |
| Sprint 6 | Runner Enhancements         | Environment-specific execution, retry mechanism, tag filtering |
| Sprint 7 | Scaling & Parallelism       | Multiple worker setup, fair dispatch, isolated reports   |
| Sprint 8 | Final Integration & Dashboard | Persistent DB, REST APIs for querying runs, optional UI dashboard, CI/CD ready |

## 6. Standup & Progress Tracking
*   **Daily Standups:** Updates on yesterday’s work, today’s plan, blockers.
*   **Sprint Review:** Demonstrate completed features.
*   **Sprint Retrospective:** Identify improvements for next sprint.
*   **Burndown Charts & Metrics:** Track story points and velocity.

## 7. Reporting & Metrics
*   Pass/fail count, execution time, flakiness detection.
*   Detailed logs and failure screenshots.
*   Sprint velocity and backlog progress.
*   CI/CD pipeline integration for automated feedback.

## 8. Risks & Mitigation
| Risk                      | Impact | Mitigation                                       |
| ------------------------- | ------ | ------------------------------------------------ |
| Worker crash mid-run      | High   | Retry, DLQ, logging                              |
| Parallel execution conflicts | Medium | Isolated reports, thread-safe execution          |
| RabbitMQ downtime         | High   | Docker health checks, restart policies           |
| Flaky UI tests            | Medium | Retry mechanism, screenshots for debugging       |
| DB schema issues          | Medium | Version-controlled migrations                    |

## 9. Project Outcomes
*   **Efficient Automation:** Parallel Selenium/REST-Assured execution.
*   **Automated Management:** Test scheduling and execution via API.
*   **Comprehensive Reporting:** HTML/CSV/JUnit reports, failure screenshots, logs.
*   **Reliability & Scalability:** Multiple workers, persistent DB, CI/CD ready framework.
