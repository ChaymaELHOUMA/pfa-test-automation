package com.smartautomation.testautomation.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${llm.api.base-url}")
    private String baseUrl;

    @Value("${llm.api.key}")
    private String apiKey;

    @Bean
    public WebClient llmWebClient() {
    
    return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .build();
}

    public String getApiKey() {
    return apiKey;
}
}

