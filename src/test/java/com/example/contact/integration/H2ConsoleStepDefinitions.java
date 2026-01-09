package com.example.contact.integration;

import com.example.contact.CucumberSpringConfiguration;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.當;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class H2ConsoleStepDefinitions extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<String> h2Response;

    @假設("系統以開發環境配置運行")
    public void systemRunningInDevMode() {
        // The test runs with dev profile by default
    }

    @當("開發人員存取 H2 控制台路徑")
    public void accessH2Console() {
        h2Response = restTemplate.getForEntity(
            getBaseUrl() + "/h2-console",
            String.class
        );
    }

    @那麼("系統回傳 H2 控制台頁面")
    public void returnH2ConsolePage() {
        // H2 console redirects or returns HTML
        assertThat(h2Response.getStatusCode())
            .satisfiesAnyOf(
                status -> assertThat(status).isEqualTo(HttpStatus.OK),
                status -> assertThat(status).isEqualTo(HttpStatus.FOUND),
                status -> assertThat(status.is3xxRedirection()).isTrue()
            );
    }
}
