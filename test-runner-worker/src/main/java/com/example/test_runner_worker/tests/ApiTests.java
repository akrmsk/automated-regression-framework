package com.example.test_runner_worker.tests;


import org.springframework.stereotype.Service;

import static io.restassured.RestAssured.given;

@Service
public class ApiTests {
    public void runApiSmokeTest() {
        // We'll use a free, simple, public API for this test
        String publicApiUrl = "https://api.publicapis.org/entries";

        // This is the core REST-Assured test
        // It will automatically throw an 'AssertionError' if the test fails
        given()
                .param("title", "cat") // Add a query param
                .when()
                .get(publicApiUrl)
                .then()
                .statusCode(200); // Verify the status code is 200 OK
    }
}
