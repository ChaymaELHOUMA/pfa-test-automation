package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.*;
import com.smartautomation.testautomation.model.MultiTypeTestResult.CategorieMetrics;
import com.smartautomation.testautomation.model.MultiTypeTestResult.FichierDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class MultiTypeGenerationService {

    private static final Logger log = LoggerFactory.getLogger(MultiTypeGenerationService.class);

    private final AstAnalysisService astAnalysisService;
    private final CodeSnippetExtractor codeSnippetExtractor;
    private final PromptBuilderService promptBuilderService;
    private final ProjectService projectService;
    private final LlmService llmService;
    private final TestCodeExtractor testCodeExtractor;
    private final TestDeduplicationService testDeduplicationService;
    private final TestFileWriterService testFileWriterService;
    private final JestExecutionService jestExecutionService;
    private final MultiTypeTestExecutionService multiTypeTestExecutionService;

    public MultiTypeGenerationService(
            AstAnalysisService astAnalysisService,
            CodeSnippetExtractor codeSnippetExtractor,
            PromptBuilderService promptBuilderService,
            ProjectService projectService,
            LlmService llmService,
            TestCodeExtractor testCodeExtractor,
            TestDeduplicationService testDeduplicationService,
            TestFileWriterService testFileWriterService,
            JestExecutionService jestExecutionService,
            MultiTypeTestExecutionService multiTypeTestExecutionService) {
        this.astAnalysisService = astAnalysisService;
        this.codeSnippetExtractor = codeSnippetExtractor;
        this.promptBuilderService = promptBuilderService;
        this.projectService = projectService;
        this.llmService = llmService;
        this.testCodeExtractor = testCodeExtractor;
        this.testDeduplicationService = testDeduplicationService;
        this.testFileWriterService = testFileWriterService;
        this.jestExecutionService = jestExecutionService;
        this.multiTypeTestExecutionService = multiTypeTestExecutionService;
    }

    public MultiTypeTestResult genererSuiteMultiType(String chemin, String idProjet) {
        String projetIdFinal = (idProjet != null && !idProjet.isBlank()) ? idProjet : "projet-reference";
        String cheminResolu = projectService.resoudreCheminFichier(projetIdFinal, chemin);

        log.info("Démarrage de la génération multi-type pour le fichier: {}, projet: {}", cheminResolu, projetIdFinal);

        AstAnalysisOutcome astOutcome = astAnalysisService.analyser(cheminResolu);
        if (astOutcome.isErreurTechnique() || astOutcome.getResultat() == null) {
            String msg = astOutcome.isErreurTechnique() ? astOutcome.getMessageErreur() : "AST vide";
            log.warn("Échec de l'analyse AST pour {}: {}", cheminResolu, msg);
            return new MultiTypeTestResult(0, 0, 0, 0.0, Map.of(), List.of(
                    new FichierDetail(chemin, "AST_ERROR", 0, 0, 0, false, "Erreur AST: " + msg)
            ));
        }

        List<FichierDetail> details = new ArrayList<>();
        Map<String, int[]> statsParType = new HashMap<>();
        statsParType.put("UNITAIRE", new int[]{0, 0, 0});
        statsParType.put("INTEGRATION", new int[]{0, 0, 0});
        statsParType.put("API", new int[]{0, 0, 0});

        int totalGlobal = 0;
        int reussisGlobal = 0;
        int echouesGlobal = 0;

        List<UniteTestable> unites = astOutcome.getResultat().getUnites();
        if (unites == null) unites = List.of();

        for (UniteTestable unite : unites) {
            String nomUnite = unite.getNom();
            String extraitCode;
            try {
                extraitCode = codeSnippetExtractor.extraireExtrait(cheminResolu, unite.getLigneDebut(), unite.getLigneFin());
            } catch (IOException e) {
                log.error("Impossible d'extraire le code pour {}: {}", nomUnite, e.getMessage());
                details.add(new FichierDetail(nomUnite, "EXTRACTION", 0, 0, 0, false, "Erreur extraction: " + e.getMessage()));
                continue;
            }

            List<String> typesAGenerer = determinerTypesAGenerer(unite, projetIdFinal);
            for (String type : typesAGenerer) {
                try {
                    genererEtExecuterPourType(unite, extraitCode, type, cheminResolu, projetIdFinal, details, statsParType);
                } catch (Exception e) {
                    log.error("Erreur inattendue pour l'unité {} (type {}): {}", nomUnite, type, e.getMessage());
                    details.add(new FichierDetail(nomUnite + "-" + type, "GENERATION", 0, 0, 0, false, "Erreur globale: " + e.getMessage()));
                }
            }
        }

        for (FichierDetail f : details) {
            if ("EXTRACTION".equals(f.categorie()) || "GENERATION".equals(f.categorie()) || "AST_ERROR".equals(f.categorie())) {
                continue;
            }
            totalGlobal += f.totalTests();
            reussisGlobal += f.testsReussis();
            echouesGlobal += f.testsEchoues();
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

        return new MultiTypeTestResult(totalGlobal, reussisGlobal, echouesGlobal, tauxSuccesGlobal, resultatsParType, details);
    }

    private void genererEtExecuterPourType(
            UniteTestable unite, String extraitCode, String mode,
            String cheminResolu, String projetIdFinal,
            List<FichierDetail> details, Map<String, int[]> statsParType) throws Exception {

        String nomUnite = unite.getNom();
        String nomFichierLogique = nomUnite + "-" + ("structure".equals(mode) ? "unitaire" : "integration");
        
        String prompt;
        if ("integration".equals(mode)) {
            String contexteCouches = projectService.construireContexteCouches(projetIdFinal, cheminResolu);
            prompt = promptBuilderService.construirePromptIntegration(unite, extraitCode, contexteCouches, List.of(), projetIdFinal);
        } else {
            prompt = promptBuilderService.construirePromptCible(unite, extraitCode, List.of(), projetIdFinal);
        }

        if (prompt == null) {
            details.add(new FichierDetail(nomFichierLogique, "GENERATION", 0, 0, 0, false, "Erreur: Prompt null"));
            return;
        }

        String reponseLlm;
        try {
            reponseLlm = llmService.genererDepuisPrompt(prompt);
        } catch (Exception e) {
            details.add(new FichierDetail(nomFichierLogique, "GENERATION", 0, 0, 0, false, "Erreur LLM: " + e.getMessage()));
            return;
        }

        String codeTest;
        try {
            codeTest = testCodeExtractor.extraireCode(reponseLlm);
        } catch (Exception e) {
            details.add(new FichierDetail(nomFichierLogique, "GENERATION", 0, 0, 0, false, "Erreur extraction code LLM: " + e.getMessage()));
            return;
        }

        TestDeduplicationResult dedupResult = testDeduplicationService.analyserEtDedupliquer(codeTest);
        String codeFinal = dedupResult.getCodeDeduplique();

        Path fichierTest;
        try {
            fichierTest = "projet-reference".equals(projetIdFinal)
                    ? testFileWriterService.ecrireFichierTest(nomFichierLogique, codeFinal)
                    : testFileWriterService.ecrireFichierTest(nomFichierLogique, codeFinal, projetIdFinal);
        } catch (IOException e) {
            details.add(new FichierDetail(nomFichierLogique, "GENERATION", 0, 0, 0, false, "Erreur ecriture: " + e.getMessage()));
            return;
        }

        String nomFichierEcrit = fichierTest.getFileName().toString();
        String categorie = multiTypeTestExecutionService.determinerCategorie(nomFichierEcrit);
        
        String cheminRelatifPourJest = "projet-reference".equals(projetIdFinal)
                ? testFileWriterService.cheminRelatifPourJest(nomFichierLogique)
                : testFileWriterService.cheminRelatifPourJest(nomFichierLogique, projetIdFinal);

        ExecutionTestResult execResult = jestExecutionService.executerTest(cheminRelatifPourJest);
        
        int total = 0;
        int reussis = 0;
        int echoues = 0;
        boolean succes = false;
        String msgErreur = null;

        if (execResult.isErreurTechnique()) {
            msgErreur = execResult.getMessageErreurTechnique();
            echoues = 1;
            total = 1;
        } else {
            JestRunResult jestResult = execResult.getResultatJest();
            total = jestResult.getTotalTests();
            reussis = jestResult.getTestsReussis();
            echoues = jestResult.getTestsEchoues();
            succes = jestResult.isSuccess();
        }

        details.add(new FichierDetail(nomFichierEcrit, categorie, total, reussis, echoues, succes, msgErreur));
        
        int[] stats = statsParType.computeIfAbsent(categorie, k -> new int[]{0, 0, 0});
        stats[0] += total;
        stats[1] += reussis;
        stats[2] += echoues;
    }

    private List<String> determinerTypesAGenerer(UniteTestable unite, String idProjet) {
        List<String> types = new ArrayList<>();
        types.add("structure"); // Toujours unitaire

        if (unite.isRoute()) {
            ProjectService.ProjectMetadata metadata = projectService.getProjet(idProjet);
            boolean hasRepo = metadata.repositoryFile() != null && !metadata.repositoryFile().isBlank();
            boolean hasEntry = metadata.pointEntree() != null && !metadata.pointEntree().isBlank();
            if (hasRepo || hasEntry) {
                types.add("integration");
            }
        }
        return types;
    }

    private double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
