package com.smartautomation.testautomation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartautomation.testautomation.model.MutantMetrics;
import com.smartautomation.testautomation.model.MutationResult;
import com.smartautomation.testautomation.model.NodeExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class MutationTestingService {

    private static final Logger log = LoggerFactory.getLogger(MutationTestingService.class);

    private final NodeBridgeService nodeBridgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Timeout spécifique et élevé pour la campagne de mutation Stryker (par défaut 300s / 5min) */
    @Value("${stryker.execution.timeout-seconds:300}")
    private long timeoutSeconds;

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir;

    public MutationTestingService(NodeBridgeService nodeBridgeService) {
        this.nodeBridgeService = nodeBridgeService;
    }

    public MutationResult executerMutationTesting() {
        return executerMutationTesting("projet-reference");
    }

    public MutationResult executerMutationTesting(String idProjet) {
        log.info("Lancement de la campagne de Mutation Testing (Stryker Mutator) pour le projet '{}' avec un timeout de {}s...", idProjet, timeoutSeconds);

        NodeExecutionResult result;
        if ("projet-reference".equals(idProjet) || idProjet == null || idProjet.isBlank()) {
            result = nodeBridgeService.runNpxCommand(
                    timeoutSeconds,
                    "stryker", "run"
            );
        } else {
            Path customConfig = Paths.get(scriptsDir, "workspaces", idProjet, "stryker.config.json");
            if (java.nio.file.Files.exists(customConfig)) {
                result = nodeBridgeService.runNpxCommand(
                        timeoutSeconds,
                        "stryker", "run", "workspaces/" + idProjet + "/stryker.config.json"
                );
            } else {
                result = nodeBridgeService.runNpxCommand(
                        timeoutSeconds,
                        "stryker", "run"
                );
            }
        }

        if (result.isTimedOut()) {
            log.warn("Timeout dépassé lors de la campagne de mutation testing ({}s)", timeoutSeconds);
            return MutationResult.erreur("Timeout dépassé lors de l'exécution de Stryker (" + timeoutSeconds + "s)");
        }

        File reportFile;
        if ("projet-reference".equals(idProjet) || idProjet == null || idProjet.isBlank()) {
            reportFile = new File(scriptsDir, "reports/mutation/mutation.json");
        } else {
            reportFile = new File(scriptsDir, "workspaces/" + idProjet + "/reports/mutation/mutation.json");
            if (!reportFile.exists()) {
                reportFile = new File(scriptsDir, "reports/mutation/mutation.json");
            }
        }

        if (!reportFile.exists()) {
            log.error("Fichier de rapport Stryker introuvable : {}", reportFile.getAbsolutePath());
            return MutationResult.erreur("Rapport Stryker mutation.json non produit. stderr: " + result.getStderr());
        }

        try {
            return parserRapportStryker(reportFile);
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du rapport Stryker JSON {}", reportFile.getAbsolutePath(), e);
            return MutationResult.erreur("Erreur de parsing du rapport Stryker JSON : " + e.getMessage());
        }
    }

    public MutationResult parserRapportStryker(File jsonFile) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        return parserJsonNode(rootNode);
    }

    public MutationResult parserRapportStryker(String jsonContent) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        return parserJsonNode(rootNode);
    }

    private MutationResult parserJsonNode(JsonNode rootNode) {
        if (rootNode == null || !rootNode.has("files")) {
            return MutationResult.erreur("Le rapport JSON Stryker est invalide ou ne contient pas la clé 'files'");
        }

        int totalKilled = 0;
        int totalSurvived = 0;
        int totalTimedOut = 0;
        int totalNoCoverage = 0;
        int totalCompileError = 0;

        Map<String, MutantMetrics> metricsParFichier = new HashMap<>();

        JsonNode filesNode = rootNode.get("files");
        Iterator<Map.Entry<String, JsonNode>> fields = filesNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fileName = entry.getKey();
            JsonNode fileData = entry.getValue();

            int killed = 0;
            int survived = 0;
            int timedOut = 0;
            int noCoverage = 0;
            int compileError = 0;

            if (fileData.has("mutants") && fileData.get("mutants").isArray()) {
                for (JsonNode mutant : fileData.get("mutants")) {
                    String status = mutant.has("status") ? mutant.get("status").asText() : "";
                    switch (status) {
                        case "Killed":
                            killed++;
                            break;
                        case "Survived":
                            survived++;
                            break;
                        case "TimedOut":
                        case "Timeout":
                            timedOut++;
                            break;
                        case "NoCoverage":
                            noCoverage++;
                            break;
                        case "CompileError":
                        case "Ignored":
                        default:
                            compileError++;
                            break;
                    }
                }
            }

            int totalFile = killed + survived + timedOut + noCoverage + compileError;
            double scoreFile = calculerScore(killed, timedOut, survived, noCoverage);

            metricsParFichier.put(fileName, new MutantMetrics(killed, survived, timedOut, noCoverage, compileError, totalFile, scoreFile));

            totalKilled += killed;
            totalSurvived += survived;
            totalTimedOut += timedOut;
            totalNoCoverage += noCoverage;
            totalCompileError += compileError;
        }

        int totalGlobal = totalKilled + totalSurvived + totalTimedOut + totalNoCoverage + totalCompileError;
        double scoreGlobal = calculerScore(totalKilled, totalTimedOut, totalSurvived, totalNoCoverage);

        MutantMetrics globalMetrics = new MutantMetrics(totalKilled, totalSurvived, totalTimedOut, totalNoCoverage, totalCompileError, totalGlobal, scoreGlobal);

        return MutationResult.succes(globalMetrics, metricsParFichier);
    }

    private double calculerScore(int killed, int timedOut, int survived, int noCoverage) {
        int dénominateur = killed + timedOut + survived + noCoverage;
        if (dénominateur == 0) {
            return 0.0;
        }
        double score = ((double) (killed + timedOut) / dénominateur) * 100.0;
        return Math.round(score * 100.0) / 100.0;
    }
}
