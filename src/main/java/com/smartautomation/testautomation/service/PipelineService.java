package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.exception.LlmCallException;
import com.smartautomation.testautomation.exception.TestGenerationException;
import com.smartautomation.testautomation.model.AstAnalysisOutcome;
import com.smartautomation.testautomation.model.ExecutionTestResult;
import com.smartautomation.testautomation.model.JestRunResult;
import com.smartautomation.testautomation.model.PipelineResult;
import com.smartautomation.testautomation.model.UniteTestable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class PipelineService {

    private static final Logger log = LoggerFactory.getLogger(PipelineService.class);

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir;

    private final LlmService llmService;
    private final TestCodeExtractor extractor;
    private final TestFileWriterService fileWriter;
    private final JestExecutionService jestExecutionService;
    private final AstAnalysisService astAnalysisService;
    private final CodeSnippetExtractor snippetExtractor;
    private final PromptBuilderService promptBuilderService;
    private final ProjectService projectService;

    public PipelineService(LlmService llmService,
                            TestCodeExtractor extractor,
                            TestFileWriterService fileWriter,
                            JestExecutionService jestExecutionService,
                            AstAnalysisService astAnalysisService,
                            CodeSnippetExtractor snippetExtractor,
                            PromptBuilderService promptBuilderService,
                            ProjectService projectService) {
        this.llmService = llmService;
        this.extractor = extractor;
        this.fileWriter = fileWriter;
        this.jestExecutionService = jestExecutionService;
        this.astAnalysisService = astAnalysisService;
        this.snippetExtractor = snippetExtractor;
        this.promptBuilderService = promptBuilderService;
        this.projectService = projectService;
    }

    /**
     * Point d'entree principal du pipeline AST-structure.
     *
     * @param chemin    Chemin relatif du fichier source (ex: "routes-utilisateurs.js")
     * @param nomUnite  Nom de l'unite testable a cibler (ex: "validerEmail" ou "POST /users")
     * @param mode      Strategie de prompt : "structure", "brut" ou "integration"
     * @param idProjet  Identifiant du projet (ex: "projet-reference")
     */
    public PipelineResult genererEcrireEtExecuter(String chemin, String nomUnite, String mode, String idProjet) {
        String projetIdFinal = (idProjet != null && !idProjet.isBlank()) ? idProjet : "projet-reference";
        String modeFinal = (mode != null && !mode.isBlank()) ? mode.toLowerCase() : "structure";

        // a. Resolution du chemin reel via ProjectService
        String cheminResolu = projectService.resoudreCheminFichier(projetIdFinal, chemin);
        log.info("Pipeline AST pour unite='{}' dans '{}' (projet={}, mode={})",
                nomUnite, cheminResolu, projetIdFinal, modeFinal);

        // b. Analyse AST du fichier
        AstAnalysisOutcome outcome = astAnalysisService.analyser(cheminResolu);
        if (outcome.isErreurTechnique()) {
            log.warn("Echec analyse AST pour {} : {}", cheminResolu, outcome.getMessageErreur());
            return PipelineResult.echecGeneration("Erreur AST : " + outcome.getMessageErreur());
        }

        // c. Recherche de l'unite par nom
        Optional<UniteTestable> uniteOpt = outcome.getResultat().getUnites().stream()
                .filter(u -> nomUnite.equals(u.getNom()))
                .findFirst();

        if (uniteOpt.isEmpty()) {
            String msg = "Unite '" + nomUnite + "' introuvable dans " + cheminResolu;
            log.warn(msg);
            return PipelineResult.echecGeneration(msg);
        }

        UniteTestable unite = uniteOpt.get();

        // d. Extraction du vrai code source via CodeSnippetExtractor
        String extraitCodeSource;
        try {
            extraitCodeSource = snippetExtractor.extraireExtrait(cheminResolu, unite.getLigneDebut(), unite.getLigneFin());
        } catch (IOException e) {
            log.error("Impossible de lire le code source de '{}' dans {}", nomUnite, cheminResolu, e);
            return PipelineResult.echecGeneration("Impossible de lire le fichier source : " + e.getMessage());
        }

        // e. Construction du prompt structure selon le mode
        String prompt;
        try {
            prompt = switch (modeFinal) {
                case "brut" -> promptBuilderService.construirePromptBrut(unite, extraitCodeSource, List.of(), projetIdFinal);
                case "integration" -> {
                    if (!unite.isRoute()) {
                        yield null;
                    }
                    // Lecture reelle des couches metier via ProjectService
                    String contexteCouches = projectService.construireContexteCouches(projetIdFinal, cheminResolu);
                    yield promptBuilderService.construirePromptIntegration(unite, extraitCodeSource, contexteCouches, List.of(), projetIdFinal);
                }
                default -> promptBuilderService.construirePromptCible(unite, extraitCodeSource, List.of(), projetIdFinal);
            };
        } catch (Exception e) {
            log.error("Erreur lors de la construction du prompt pour '{}' (mode={})", nomUnite, modeFinal, e);
            return PipelineResult.echecGeneration("Erreur construction prompt : " + e.getMessage());
        }

        if (prompt == null) {
            String msg = "Le mode 'integration' ne s'applique qu'aux routes Express (unite '" + nomUnite + "' n'est pas une route)";
            log.warn(msg);
            return PipelineResult.echecGeneration(msg);
        }

        // f. Appel LLM avec le prompt structure (PAS genererTest)
        String reponseLlm;
        try {
            reponseLlm = llmService.genererDepuisPrompt(prompt);
        } catch (LlmCallException e) {
            log.warn("Echec de l'appel LLM pour '{}' : {}", nomUnite, e.getMessage());
            return PipelineResult.echecGeneration("Erreur LLM : " + e.getMessage());
        }

        // g. Extraction du bloc de code depuis la reponse LLM
        String codeTest;
        try {
            codeTest = extractor.extraireCode(reponseLlm);
        } catch (TestGenerationException e) {
            log.warn("Reponse LLM invalide pour '{}' : {}", nomUnite, e.getMessage());
            return PipelineResult.echecGeneration("Reponse LLM invalide : " + e.getMessage());
        }

        // h. Ecriture du fichier de test
        Path fichier;
        try {
            fichier = "projet-reference".equals(projetIdFinal)
                    ? fileWriter.ecrireFichierTest(nomUnite, codeTest)
                    : fileWriter.ecrireFichierTest(nomUnite, codeTest, projetIdFinal);
        } catch (IOException e) {
            log.error("Erreur d'ecriture du fichier de test pour '{}'", nomUnite, e);
            return PipelineResult.echecGeneration("Erreur ecriture fichier : " + e.getMessage());
        }

        // i. Execution Jest
        String cheminRelatif = "projet-reference".equals(projetIdFinal)
                ? fileWriter.cheminRelatifPourJest(nomUnite)
                : fileWriter.cheminRelatifPourJest(nomUnite, projetIdFinal);

        ExecutionTestResult resultatExecution = jestExecutionService.executerTest(cheminRelatif);

        if (resultatExecution.isErreurTechnique()) {
            log.error("Erreur technique lors de l'execution du test {} : {}",
                    fichier, resultatExecution.getMessageErreurTechnique());
            return PipelineResult.echecExecution(fichier.toString(), codeTest, resultatExecution.getMessageErreurTechnique());
        }

        JestRunResult resultatJest = resultatExecution.getResultatJest();
        log.info("Pipeline complet pour '{}' sur projet '{}' : {}/{} tests reussis",
                nomUnite, projetIdFinal, resultatJest.getTestsReussis(), resultatJest.getTotalTests());

        return PipelineResult.succes(
                fichier.toString(),
                codeTest,
                resultatJest.getTotalTests(),
                resultatJest.getTestsReussis(),
                resultatJest.getTestsEchoues()
        );
    }
}
