package com.smartautomation.testautomation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartautomation.testautomation.model.CoverageMetrics;
import com.smartautomation.testautomation.model.CoverageResult;
import com.smartautomation.testautomation.model.CoverageResult.FileCoverageSummary;
import com.smartautomation.testautomation.model.NodeExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class CoverageService {

    private static final Logger log = LoggerFactory.getLogger(CoverageService.class);

    private final NodeBridgeService nodeBridgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jest.execution.timeout-seconds:60}")
    private long timeoutSeconds;

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir;

    public CoverageService(NodeBridgeService nodeBridgeService) {
        this.nodeBridgeService = nodeBridgeService;
    }

    public CoverageResult mesurerCouvertureProjet(String... testFiles) {
        return mesurerCouverturePourProjet("projet-reference", testFiles);
    }

    public CoverageResult mesurerCouverturePourProjet(String idProjet, String... testFiles) {
        log.info("Lancement de la mesure de couverture (nyc/Istanbul/Jest) pour le projet '{}'", idProjet);

        List<String> argsList = new ArrayList<>();
        if (testFiles != null && testFiles.length > 0) {
            for (String file : testFiles) {
                if (file != null && !file.isBlank()) {
                    argsList.add(file);
                }
            }
        }

        if ("projet-reference".equals(idProjet) || idProjet == null || idProjet.isBlank()) {
            log.info("Utilisation volontaire du projet de référence pour la mesure de couverture.");
        } else {
            Path customConfig = Paths.get(scriptsDir, "workspaces", idProjet, "jest.config.json");
            if (java.nio.file.Files.exists(customConfig)) {
                argsList.add("--config");
                argsList.add("workspaces/" + idProjet + "/jest.config.json");
            } else {
                log.error("Configuration Jest introuvable pour le projet {}", idProjet);
                return CoverageResult.erreur("Configuration introuvable pour le projet " + idProjet + " — vérifiez que le projet a bien été uploadé et initialisé.");
            }
        }

        argsList.add("--coverage");
        argsList.add("--coverageReporter=json-summary");
        argsList.add("--coverageReporter=text");
        argsList.add("--ci");
        argsList.add("--forceExit");

        NodeExecutionResult result = nodeBridgeService.runScriptAtPathWithTimeout(
                "node_modules/jest/bin/jest.js",
                timeoutSeconds,
                argsList.toArray(new String[0])
        );

        if (result.isTimedOut()) {
            log.warn("Timeout dépassé lors du calcul de couverture");
            return CoverageResult.erreur("Timeout dépassé (" + timeoutSeconds + "s)");
        }

        File summaryFile;
        if ("projet-reference".equals(idProjet) || idProjet == null || idProjet.isBlank()) {
            summaryFile = new File(scriptsDir, "coverage/coverage-summary.json");
        } else {
            summaryFile = new File(scriptsDir, "workspaces/" + idProjet + "/coverage/coverage-summary.json");
        }

        if (!summaryFile.exists()) {
            log.error("Fichier de couverture introuvable : {}", summaryFile.getAbsolutePath());
            return CoverageResult.erreur("Rapport de couverture introuvable pour le projet " + idProjet + " (fichier " + summaryFile.getName() + " non produit). stderr: " + result.getStderr());
        }

        try {
            return parserRapportCouverture(summaryFile);
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du rapport de couverture {}", summaryFile.getAbsolutePath(), e);
            return CoverageResult.erreur("Erreur de parsing du rapport JSON : " + e.getMessage());
        }
    }

    public CoverageResult parserRapportCouverture(File jsonFile) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        return parserJsonNode(rootNode);
    }

    public CoverageResult parserRapportCouverture(String jsonString) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonString);
        return parserJsonNode(rootNode);
    }

    private CoverageResult parserJsonNode(JsonNode rootNode) {
        if (rootNode == null || !rootNode.has("total")) {
            return CoverageResult.erreur("Le JSON de couverture ne contient pas le noeud 'total'");
        }

        JsonNode totalNode = rootNode.get("total");
        CoverageMetrics lines = extraireMetrics(totalNode.get("lines"));
        CoverageMetrics branches = extraireMetrics(totalNode.get("branches"));
        CoverageMetrics statements = extraireMetrics(totalNode.get("statements"));
        CoverageMetrics functions = extraireMetrics(totalNode.get("functions"));

        Map<String, FileCoverageSummary> mapFichiers = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            if ("total".equals(key)) {
                continue;
            }
            JsonNode fileNode = entry.getValue();
            CoverageMetrics fLines = extraireMetrics(fileNode.get("lines"));
            CoverageMetrics fBranches = extraireMetrics(fileNode.get("branches"));
            CoverageMetrics fStatements = extraireMetrics(fileNode.get("statements"));
            CoverageMetrics fFunctions = extraireMetrics(fileNode.get("functions"));

            mapFichiers.put(key, new FileCoverageSummary(fLines, fBranches, fStatements, fFunctions));
        }

        return CoverageResult.succes(lines, branches, statements, functions, mapFichiers);
    }

    private CoverageMetrics extraireMetrics(JsonNode node) {
        if (node == null) {
            return new CoverageMetrics(0, 0, 0, 0.0);
        }
        int total = node.has("total") ? node.get("total").asInt() : 0;
        int covered = node.has("covered") ? node.get("covered").asInt() : 0;
        int skipped = node.has("skipped") ? node.get("skipped").asInt() : 0;
        double pct = node.has("pct") ? node.get("pct").asDouble() : 0.0;
        return new CoverageMetrics(total, covered, skipped, pct);
    }
}
