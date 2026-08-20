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
                Voici une fonction JavaScript nommée %s :

                %s

                Génère des tests unitaires Jest distincts pour cette fonction, couvrant le cas nominal, les cas limites (ex: 0, "", [], {}) et les cas d'erreur/invalides.

                FORMAT DE NOMMAGE STRICTEMENT OBLIGATOIRE :
                - test('cas nominal - <description>', () => { ... })
                - test('cas limite - <description>', () => { ... })
                - test('cas invalide - <description>', () => { ... })
                - test('cas erreur - <description>', () => { ... })

                CONSIGNES DE COMPILATION ET SYNTAXE JEST (TRÈS IMPORTANT) :
                - Pour tester qu'une fonction synchrone lève une erreur (throw), enveloppe TOUJOURS l'appel dans une fonction fléchée : expect(() => %s(...)).toThrow();
                - Le fichier de test doit être 100%% autonome et directement exécutable.
                - Inclus la définition complète et exacte de la fonction directement en haut du fichier de test.
                - Ne fais AUCUN require ni import vers un fichier externe.

                Réponds uniquement avec un bloc de code JavaScript délimité par
                ```javascript
                et
                ```,
                sans aucune explication avant ou après le bloc.
                """.formatted(nomFonction, codeSource, nomFonction);
    }

    public String genererDepuisPrompt(String prompt) {
        LlmRequest requete = new LlmRequest(prompt);

        String uri = String.format(
                "/v1beta/models/%s:generateContent?key=%s",
                model,
                webClientConfig.getApiKey()
        );

        try {
            LlmResponse reponse = webClient.post()
                    .uri(uri)
                    .bodyValue(requete)
                    .retrieve()
                    .bodyToMono(LlmResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (reponse == null
                    || reponse.getTexteComplet() == null
                    || reponse.getTexteComplet().isBlank()) {

                throw new LlmCallException(
                        "Le LLM a renvoyé une réponse vide"
                );
            }

            return reponse.getTexteComplet();

        } catch (WebClientResponseException e) {

            log.error(
                    "Erreur HTTP {} lors de l'appel au LLM : {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new LlmCallException(
                    "Erreur HTTP du LLM : " + e.getStatusCode(),
                    e
            );

        } catch (WebClientRequestException e) {

            log.error(
                    "Erreur réseau lors de l'appel au LLM",
                    e
            );

            throw new LlmCallException(
                    "Erreur réseau lors de l'appel au LLM : " + e.getMessage(),
                    e
            );

        } catch (RuntimeException e) {

            if (e.getCause() instanceof TimeoutException) {

                log.error(
                        "Timeout ({}s) lors de l'appel au LLM",
                        timeoutSeconds
                );

                throw new LlmCallException(
                        "Timeout dépassé lors de l'appel au LLM",
                        e
                );
            }

            log.error(
                    "Erreur inattendue lors de l'appel au LLM",
                    e
            );

            throw new LlmCallException(
                    "Erreur inattendue lors de l'appel au LLM : " + e.getMessage(),
                    e
            );
        }
    }
}
