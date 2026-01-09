package com.example.contact.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("聯絡人管理系統 API")
                .description("""
                    聯絡人管理系統 RESTful API 文件

                    ## 功能特色
                    - 完整的 CRUD 操作支援
                    - 自動稽核日誌記錄（透過 AOP）
                    - 稽核日誌查詢功能
                    - 六角形架構設計

                    ## 技術棧
                    - Java 17
                    - Spring Boot 3.2
                    - Spring Data JPA
                    - H2 Database (開發環境)
                    - PostgreSQL (生產環境)
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("開發團隊")
                    .email("dev@example.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("本地開發環境")
            ));
    }
}
