package com.InfraDesk.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI ticketAnalyticsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("InfraDesk Ticket Analytics API")
                        .description("Comprehensive analytics and reporting API for ticket management system")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("InfraDesk Support")
                                .email("support@infradesk.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

