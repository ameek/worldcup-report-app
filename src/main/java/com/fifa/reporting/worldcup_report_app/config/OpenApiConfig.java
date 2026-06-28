package com.fifa.reporting.worldcup_report_app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI worldCupOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FIFA World Cup Reporting API")
                        .description("REST API for World Cup data access and JasperReports PDF generation")
                        .version("v1")
                        .contact(new Contact()
                                .name("FIFA Reporting Team")
                                .email("support@fifa-reporting.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")));
    }
}
