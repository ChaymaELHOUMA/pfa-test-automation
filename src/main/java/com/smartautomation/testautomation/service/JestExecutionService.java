package com.smartautomation.testautomation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartautomation.testautomation.model.ExecutionTestResult;
import com.smartautomation.testautomation.model.JestRunResult;
import com.smartautomation.testautomation.model.NodeExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JestExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JestExecutionService.class);

    private final NodeBridgeService nodeBridgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jest.execution.timeout-seconds:45}")
    private long timeoutSeconds;

    public JestExecutionService(NodeBridgeService nodeBridgeService) {
        this.nodeBridgeService = nodeBridgeService;
    }

    public ExecutionTestResult executerTest(String cheminFichierTest) {
        LOGGER.info("Execution du test genere via Jest : {}", cheminFichierTest);

        NodeExecutionResult resultatProcess = nodeBridgeService.runScriptAtPathWithTimeout(
                "node_modules/jest/bin/jest.js",
                timeoutSeconds,
                cheminFichierTest, "--json", "--ci", "--forceExit"
        );

        JestRunResult resultatJest = parserSortieJest(resultatProcess.getStdout(), resultatProcess.getStderr());
        if (resultatJest != null) {
            LOGGER.info("Resultat Jest pour {} : {}/{} tests reussis (success={}, timedOut={})",
                    cheminFichierTest,
                    resultatJest.getTestsReussis(),
                    resultatJest.getTotalTests(),
                    resultatJest.isSuccess(),
                    resultatProcess.isTimedOut());
            return ExecutionTestResult.depuisJest(resultatJest);
        }

        if (resultatProcess.isTimedOut()) {
            LOGGER.warn("Timeout lors de l execution du test {} (possible boucle infinie generee par le LLM)", cheminFichierTest);
            return ExecutionTestResult.erreurTechnique(
                    "Timeout depasse (" + timeoutSeconds + "s) : le test genere ne s est pas termine a temps");
        }

        String stdout = resultatProcess.getStdout();
        if (stdout == null || stdout.isBlank()) {
            LOGGER.error("Aucune sortie JSON recue de Jest pour {} : {}", cheminFichierTest, resultatProcess.getStderr());
            return ExecutionTestResult.erreurTechnique(
                    "Jest n a produit aucune sortie exploitable. stderr : " + tronquer(resultatProcess.getStderr()));
        }

        LOGGER.error("Impossible de parser la sortie JSON de Jest pour {}", cheminFichierTest);
        return ExecutionTestResult.erreurTechnique(
                "Sortie de Jest illisible (pas un JSON valide) : " + tronquer(stdout));
    }

    JestRunResult parserSortieJest(String stdout, String stderr) {
        JestRunResult depuisStdout = parserJsonJest(extraireJsonJest(stdout));
        if (depuisStdout != null) {
            return depuisStdout;
        }
        return parserJsonJest(extraireJsonJest(stderr));
    }

    private JestRunResult parserJsonJest(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JestRunResult resultat = objectMapper.readValue(json, JestRunResult.class);
            enrichirComptages(resultat);
            return resultat;
        } catch (JsonProcessingException e) {
            LOGGER.debug("Flux Jest non parsable en JSON : {}", e.getMessage());
            return null;
        }
    }

    String extraireJsonJest(String sortie) {
        if (sortie == null || sortie.isBlank()) {
            return null;
        }
        String nettoye = sortie.strip();
        if (nettoye.startsWith("\uFEFF")) {
            nettoye = nettoye.substring(1).strip();
        }
        int debut = nettoye.indexOf('{');
        int fin = nettoye.lastIndexOf('}');
        if (debut >= 0 && fin > debut) {
            return nettoye.substring(debut, fin + 1);
        }
        return nettoye;
    }

    private void enrichirComptages(JestRunResult resultat) {
        int reussisDepuisAssertions = compterAssertionsParStatut(resultat, "passed");
        int totalDepuisAssertions = compterTotalAssertions(resultat);

        if (resultat.getTestsReussis() == 0 && reussisDepuisAssertions > 0) {
            resultat.setTestsReussis(reussisDepuisAssertions);
        }
        if (resultat.getTotalTests() == 0 && totalDepuisAssertions > 0) {
            resultat.setTotalTests(totalDepuisAssertions);
        }
    }

    private int compterAssertionsParStatut(JestRunResult resultat, String statutAttendu) {
        if (resultat.getTestResults() == null) {
            return 0;
        }
        int count = 0;
        for (JestRunResult.TestSuiteResult suite : resultat.getTestResults()) {
            if (suite.getAssertionResults() == null) {
                continue;
            }
            for (JestRunResult.AssertionResult assertion : suite.getAssertionResults()) {
                if (statutAttendu.equals(assertion.getStatus())) {
                    count++;
                }
            }
        }
        return count;
    }

    private int compterTotalAssertions(JestRunResult resultat) {
        if (resultat.getTestResults() == null) {
            return 0;
        }
        int count = 0;
        for (JestRunResult.TestSuiteResult suite : resultat.getTestResults()) {
            if (suite.getAssertionResults() != null) {
                count += suite.getAssertionResults().size();
            }
        }
        return count;
    }

    private String tronquer(String texte) {
        if (texte == null) return "";
        return texte.length() > 500 ? texte.substring(0, 500) + "..." : texte;
    }
}
