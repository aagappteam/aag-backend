package aagapp_backend.configuration;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("aagapp_backend.controller") // Automatically scan controllers in the "aagapp_backend.controller" package
                .build();
    }

}
