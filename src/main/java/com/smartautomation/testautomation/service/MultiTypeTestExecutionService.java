package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.ExecutionTestResult;
import com.smartautomation.testautomation.model.JestRunResult;
import com.smartautomation.testautomation.model.MultiTypeTestResult;
import com.smartautomation.testautomation.model.MultiTypeTestResult.CategorieMetrics;
import com.smartautomation.testautomation.model.MultiTypeTestResult.FichierDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Service
public class MultiTypeTestExecutionService {

    private static final Logger log = LoggerFactory.getLogger(MultiTypeTestExecutionService.class);

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir = "node-runner";

    @Value("${test.output-subdir:generated-tests}")
    private String outputSubdir = "generated-tests";

    private final JestExecutionService jestExecutionService;

    public MultiTypeTestExecutionService(JestExecutionService jestExecutionService) {
        this.jestExecutionService = jestExecutionService;
    }

    public MultiTypeTestResult executerEtConsoliderSuite() {
        Path dossierTests = Path.of(scriptsDir, outputSubdir);
        if (!Files.exists(dossierTests) || !Files.isDirectory(dossierTests)) {
            log.warn("Dossier de tests introuvable : {}", dossierTests);
            return new MultiTypeTestResult(0, 0, 0, 0.0, Map.of(), List.of());
        }

        List<Path> fichiersTests = listerFichiersTests(dossierTests);
        List<FichierDetail> details = new ArrayList<>();

        Map<String, int[]> statsParType = new HashMap<>();
        statsParType.put("UNITAIRE", new int[]{0, 0, 0});     // {total, reussis, echoues}
        statsParType.put("INTEGRATION", new int[]{0, 0, 0});
        statsParType.put("API", new int[]{0, 0, 0});

        int totalGlobal = 0;
        int reussisGlobal = 0;
        int echouesGlobal = 0;

        for (Path fichier : fichiersTests) {
            String nomFichier = fichier.getFileName().toString();
            String categorie = determinerCategorie(nomFichier);
            String cheminRelatif = outputSubdir + "/" + nomFichier;

            ExecutionTestResult resultExec = jestExecutionService.executerTest(cheminRelatif);

            int total = 0;
            int reussis = 0;
            int echoues = 0;
            boolean succes = false;
            String msgErreur = null;

            if (resultExec.isErreurTechnique()) {
                msgErreur = resultExec.getMessageErreurTechnique();
                echoues = 1;
                total = 1;
            } else {
                JestRunResult jestResult = resultExec.getResultatJest();
                total = jestResult.getTotalTests();
                reussis = jestResult.getTestsReussis();
                echoues = jestResult.getTestsEchoues();
                succes = jestResult.isSuccess();
            }

            details.add(new FichierDetail(nomFichier, categorie, total, reussis, echoues, succes, msgErreur));

            totalGlobal += total;
            reussisGlobal += reussis;
            echouesGlobal += echoues;

            int[] stats = statsParType.computeIfAbsent(categorie, k -> new int[]{0, 0, 0});
            stats[0] += total;
            stats[1] += reussis;
            stats[2] += echoues;
        }

        double tauxSuccesGlobal = totalGlobal > 0 ? arrondir((double) reussisGlobal / totalGlobal * 100.0) : 0.0;

        Map<String, CategorieMetrics> resultatsParType = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> entry : statsParType.entrySet()) {
            int tot = entry.getValue()[0];
            int reu = entry.getValue()[1];
            int ech = entry.getValue()[2];
            double tx = tot > 0 ? arrondir((double) reu / tot * 100.0) : 0.0;
            resultatsParType.put(entry.getKey(), new CategorieMetrics(tot, reu, ech, tx));
        }

        log.info("Suite multi-types executee : {}/{} tests reussis ({}%)", reussisGlobal, totalGlobal, tauxSuccesGlobal);

        return new MultiTypeTestResult(totalGlobal, reussisGlobal, echouesGlobal, tauxSuccesGlobal, resultatsParType, details);
    }

    private List<Path> listerFichiersTests(Path dossier) {
        try (Stream<Path> stream = Files.list(dossier)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".test.js"))
                         .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                         .toList();
        } catch (IOException e) {
            log.error("Erreur lors du listage des fichiers de tests dans {}", dossier, e);
            return List.of();
        }
    }

    public String determinerCategorie(String nomFichier) {
        String nomLower = nomFichier.toLowerCase(Locale.ROOT);
        if (nomLower.contains("integration")) {
            return "INTEGRATION";
        } else if (nomLower.contains("supertest") ||
                   nomLower.startsWith("get_") || nomLower.startsWith("post_") ||
                   nomLower.startsWith("put_") || nomLower.startsWith("delete_")) {
            return "API";
        }
        return "UNITAIRE";
    }

    private double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
