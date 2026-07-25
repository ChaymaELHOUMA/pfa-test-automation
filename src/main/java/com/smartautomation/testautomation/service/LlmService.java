package com.smartautomation.testautomation.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.smartautomation.testautomation.exception.LlmCallException;
import com.smartautomation.testautomation.model.LlmRequest;
import com.smartautomation.testautomation.model.LlmResponse;


@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final WebClient webClient;

    @Value("${llm.api.model}")
    private String model;

    @Value("${llm.api.timeout-seconds:20}")
    private long timeoutSeconds;

    public LlmService(@Qualifier("llmWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String genererTest(String nomFonction, String codeSource) {
        String prompt = construirePromptSimple(nomFonction, codeSource);
        LlmRequest requete = new LlmRequest(model, 1024, prompt);

        try {
            LlmResponse reponse = webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(requete)
                    .retrieve()
                    .bodyToMono(LlmResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (reponse == null || reponse.getTexteComplet().isBlank()) {
                throw new LlmCallException("Le LLM a renvoye une reponse vide");
            }

            return reponse.getTexteComplet();

        } catch (WebClientResponseException e) {
            log.error("Erreur HTTP {} lors de l'appel au LLM : {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new LlmCallException("Erreur HTTP du LLM : " + e.getStatusCode(), e);

        } catch (WebClientRequestException e) {
            log.error("Erreur reseau lors de l'appel au LLM", e);
            throw new LlmCallException("Erreur reseau lors de l'appel au LLM : " + e.getMessage(), e);

        } catch (RuntimeException e) {
            
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                log.error("Timeout ({}s) lors de l'appel au LLM", timeoutSeconds);
                throw new LlmCallException("Timeout depasse lors de l'appel au LLM", e);
            }
            log.error("Erreur inattendue lors de l'appel au LLM", e);
            throw new LlmCallException("Erreur inattendue lors de l'appel au LLM : " + e.getMessage(), e);
        }
    }

    private String construirePromptSimple(String nomFonction, String codeSource) {
        return """
                Voici une fonction JavaScript nommee %s :

                %s

                Genere un test unitaire Jest pour cette fonction, couvrant au minimum le cas nominal.
                Reponds uniquement avec un bloc de code JavaScript delimite par ```javascript et ```,
                sans aucune explication avant ou apres le bloc.
                """.formatted(nomFonction, codeSource);
    }
}
