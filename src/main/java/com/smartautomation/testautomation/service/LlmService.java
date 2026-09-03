package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.config.WebClientConfig;
import com.smartautomation.testautomation.exception.LlmCallException;
import com.smartautomation.testautomation.model.LlmRequest;
import com.smartautomation.testautomation.model.LlmResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final WebClient webClient;
    private final WebClientConfig webClientConfig;

    @Value("${llm.api.model}")
    private String model;

    @Value("${llm.api.timeout-seconds:20}")
    private long timeoutSeconds;

    public LlmService(
            @Qualifier("llmWebClient") WebClient webClient,
            WebClientConfig webClientConfig) {

        this.webClient = webClient;
        this.webClientConfig = webClientConfig;
    }

    public String genererTest(String nomFonction, String codeSource) {
        String prompt = construirePromptSimple(nomFonction, codeSource);
        return genererDepuisPrompt(prompt);
    }

    private String construirePromptSimple(String nomFonction, String codeSource) {
        return """
                Here is a JavaScript function named %s:

                %s

                Generate distinct Jest unit tests for this function, covering the nominal case, edge cases (ex: 0, "", [], {}) and error/invalid cases.

                STRICTLY REQUIRED NAMING FORMAT:
                - test('nominal case - <description>', () => { ... })
                - test('edge case - <description>', () => { ... })
                - test('invalid case - <description>', () => { ... })
                - test('error case - <description>', () => { ... })

                JEST COMPILATION AND SYNTAX GUIDELINES (VERY IMPORTANT):
                - To test that a synchronous function throws an error, ALWAYS wrap the call in an arrow function: expect(() => %s(...)).toThrow();
                - The test file must be 100%% self-contained and directly executable.
                - Include the complete and exact function definition at the top of the test file.
                - Do NOT make ANY require or import to an external file.

                Respond only with a JavaScript code block delimited by
                ```javascript
                and
                ```,
                without any explanation before or after the block.
                """.formatted(nomFonction, codeSource, nomFonction);
    }

    public String genererDepuisPrompt(String prompt) {
        LlmRequest request = new LlmRequest(prompt);

        String uri = String.format(
                "/v1beta/models/%s:generateContent?key=%s",
                model,
                webClientConfig.getApiKey()
        );

        try {
            LlmResponse response = webClient.post()
                    .uri(Objects.requireNonNull(uri))
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(LlmResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null
                    || response.getTexteComplet() == null
                    || response.getTexteComplet().isBlank()) {

                throw new LlmCallException(
                        "LLM returned an empty response"
                );
            }

            return response.getTexteComplet();

        } catch (WebClientResponseException e) {

            log.error(
                    "HTTP error {} during LLM call: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new LlmCallException(
                    "LLM HTTP error: " + e.getStatusCode(),
                    e
            );

        } catch (WebClientRequestException e) {

            log.error(
                    "Network error during LLM call",
                    e
            );

            throw new LlmCallException(
                    "Network error during LLM call: " + e.getMessage(),
                    e
            );

        } catch (RuntimeException e) {

            if (e.getCause() instanceof TimeoutException) {

                log.error(
                        "Timeout ({}s) during LLM call",
                        timeoutSeconds
                );

                throw new LlmCallException(
                        "Timeout exceeded during LLM call",
                        e
                );
            }

            log.error(
                    "Unexpected error during LLM call",
                    e
            );

            throw new LlmCallException(
                    "Unexpected error during LLM call: " + e.getMessage(),
                    e
            );
        }
    }
}
