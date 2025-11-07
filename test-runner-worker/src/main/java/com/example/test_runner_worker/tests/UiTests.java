package com.example.test_runner_worker.tests;


import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
public class UiTests {

    private final String seleniumHubUrl;

    public UiTests(@Value("${selenium.hub.url}")String seleniumHubUrl) {
        this.seleniumHubUrl = seleniumHubUrl;
    }

    public void runUiSmokeTest() throws MalformedURLException {
        WebDriver driver=null;
        try{
            log.info("Connecting to Selenium Hub at: {}",seleniumHubUrl);
            driver=new RemoteWebDriver(new URL(seleniumHubUrl),new ChromeOptions());
            log.info("Driver created. Navigating to Google...");
            driver.get("https://www.google.com");
            String title = driver.getTitle();
            log.info("Page title is: {}", title);

            if(!title.contains("Google")){
                throw new AssertionError("Page title was '" + title + "', did not contain 'Google'");
            }
            log.info("UI test passed.");
        }
        finally {
            if(driver!=null) {
                log.info("driver quitting");
                driver.quit();
            }
        }
    }
}
