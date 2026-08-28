package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.AstAnalysisOutcome;
import com.smartautomation.testautomation.model.UniteTestable;
import com.smartautomation.testautomation.service.AstAnalysisService;
import com.smartautomation.testautomation.service.CodeSnippetExtractor;
import com.smartautomation.testautomation.service.ProjectService;
import com.smartautomation.testautomation.service.PromptBuilderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class PromptBuilderController {

    private final AstAnalysisService astAnalysisService;
    private final CodeSnippetExtractor snippetExtractor;
    private final PromptBuilderService promptBuilderService;
    private final ProjectService projectService;

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir;

    public PromptBuilderController(AstAnalysisService astAnalysisService,
                                    CodeSnippetExtractor snippetExtractor,
                                    PromptBuilderService promptBuilderService,
                                    ProjectService projectService) {
        this.astAnalysisService = astAnalysisService;
        this.snippetExtractor = snippetExtractor;
        this.promptBuilderService = promptBuilderService;
        this.projectService = projectService;
    }

    @GetMapping("/construire-prompt")
    public ResponseEntity<?> construirePrompt(@RequestParam String chemin,
                                              @RequestParam String nomUnite,
                                              @RequestParam(required = false, defaultValue = "structure") String mode,
                                              @RequestParam(required = false) List<String> testsExistants,
                                              @RequestParam(required = false, defaultValue = "projet-reference") String idProjet) {

        String cheminResolu = projectService.resoudreCheminFichier(idProjet, chemin);
        AstAnalysisOutcome outcome = astAnalysisService.analyser(cheminResolu);

        if (outcome.isErreurTechnique()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("erreur", outcome.getMessageErreur()));
        }

        Optional<UniteTestable> uniteOpt = outcome.getResultat().getUnites().stream()
                .filter(u -> nomUnite.equals(u.getNom()))
                .findFirst();

        if (uniteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erreur", "Unite '" + nomUnite + "' introuvable dans " + cheminResolu));
        }

        UniteTestable unite = uniteOpt.get();

        try {
            List<String> existants = testsExistants != null ? testsExistants : List.of();
            String extrait = snippetExtractor.extraireExtrait(cheminResolu, unite.getLigneDebut(), unite.getLigneFin());
            String prompt = switch (mode.toLowerCase()) {
                case "brut" -> promptBuilderService.construirePromptBrut(unite, extrait, existants, idProjet);
                case "integration" -> {
                    if (!unite.isRoute()) {
                        yield null;
                    }
                    String contexteCouches = construireContexteCouches(cheminResolu);
                    yield promptBuilderService.construirePromptIntegration(unite, extrait, contexteCouches, existants, idProjet);
                }
                default -> promptBuilderService.construirePromptCible(unite, extrait, existants, idProjet);
            };

            if (prompt == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erreur", "Le mode integration ne s'applique qu'aux routes Express"));
            }

            return ResponseEntity.ok(Map.of("prompt", prompt));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erreur", "Impossible de lire le fichier source : " + e.getMessage()));
        }
    }

    private String construireContexteCouches(String cheminFichierRoute) throws IOException {
        java.nio.file.Path dossier = java.nio.file.Path.of(scriptsDir, cheminFichierRoute).getParent();
        if (dossier == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        ajouterFichierSiPresent(sb, dossier.resolve("utilisateurService.js"), "Service");
        ajouterFichierSiPresent(sb, dossier.resolve("utilisateurRepository.js"), "Repository");
        return sb.toString();
    }

    private void ajouterFichierSiPresent(StringBuilder sb, java.nio.file.Path fichier, String etiquette) throws IOException {
        if (!java.nio.file.Files.exists(fichier)) {
            return;
        }
        sb.append("--- ").append(etiquette).append(" (").append(fichier.getFileName()).append(") ---")
          .append(System.lineSeparator());
        sb.append(java.nio.file.Files.readString(fichier, java.nio.charset.StandardCharsets.UTF_8))
          .append(System.lineSeparator()).append(System.lineSeparator());
    }
}
