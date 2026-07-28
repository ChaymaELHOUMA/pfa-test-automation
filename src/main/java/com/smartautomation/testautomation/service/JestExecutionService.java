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

        NodeExecutionResult resultatProcess = nodeBridgeService.runNpxCommand(
                timeoutSeconds,
                "jest", cheminFichierTest, "--json", "--ci"
        );

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

        try {
            JestRunResult resultatJest = objectMapper.readValue(stdout, JestRunResult.class);
            LOGGER.info("Resultat Jest pour {} : {}/{} tests reussis (success={})",
                    cheminFichierTest, resultatJest.getTestsReussis(), resultatJest.getTotalTests(), resultatJest.isSuccess());
            return ExecutionTestResult.depuisJest(resultatJest);
        } catch (JsonProcessingException e) {
            LOGGER.error("Impossible de parser la sortie JSON de Jest pour {}", cheminFichierTest, e);
            return ExecutionTestResult.erreurTechnique(
                    "Sortie de Jest illisible (pas un JSON valide) : " + tronquer(stdout));
        }
    }

    private String tronquer(String texte) {
        if (texte == null) return "";
        return texte.length() > 500 ? texte.substring(0, 500) + "..." : texte;
    }
}