package com.eevee.usageservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Aligns JSON handling with {@code proxy-service} ({@code JacksonConfiguration}: ObjectMapper + JSR-310 modules).
 * Spring Boot 4 does not register a {@link ObjectMapper} bean by default for this stack; the listener requires one.
 */
@Configuration
public class UsageJacksonConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
}
