package com.campusconnect.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.llm")
public class AiLlmProperties {

    private Boolean enabled = false;

    private String baseUrl;

    private String apiKey;

    private String model;
}