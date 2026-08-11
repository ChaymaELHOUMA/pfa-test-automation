package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.AstAnalysisOutcome;
import com.smartautomation.testautomation.model.UniteTestable;
import com.smartautomation.testautomation.service.AstAnalysisService;
import com.smartautomation.testautomation.service.CodeSnippetExtractor;
import com.smartautomation.testautomation.service.PromptBuilderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Endpoint de test isole : construit et affiche le prompt cible
 * SANS appeler le LLM, pour verifier son contenu avant de l'utiliser
 * reellement (meme demarche que la validation de analyze.js en ligne
 * de commande avant son integration Java).
 */
@RestController
public class PromptBuilderController {

    private final AstAnalysisService astAnalysisService;
    private final CodeSnippetExtractor snippetExtractor;
    private final PromptBuilderService promptBuilderService;

    public PromptBuilderController(AstAnalysisService astAnalysisService,
                                    CodeSnippetExtractor snippetExtractor,
                                    PromptBuilderService promptBuilderService) {
        this.astAnalysisService = astAnalysisService;
        this.snippetExtractor = snippetExtractor;
        this.promptBuilderService = promptBuilderService;
    }

    /**
     * GET /construire-prompt?chemin=projet-reference/routes-utilisateurs.js&nomUnite=GET /users/:id
     */
    @GetMapping("/construire-prompt")
    public ResponseEntity<?> construirePrompt(@RequestParam String chemin, @RequestParam String nomUnite) {
        AstAnalysisOutcome outcome = astAnalysisService.analyser(chemin);

        if (outcome.isErreurTechnique()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("erreur", outcome.getMessageErreur()));
        }

        Optional<UniteTestable> uniteOpt = outcome.getResultat().getUnites().stream()
                .filter(u -> nomUnite.equals(u.getNom()))
                .findFirst();

        if (uniteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erreur", "Unite '" + nomUnite + "' introuvable dans " + chemin));
        }

        UniteTestable unite = uniteOpt.get();

        try {
            String extrait = snippetExtractor.extraireExtrait(chemin, unite.getLigneDebut(), unite.getLigneFin());
            String prompt = promptBuilderService.construirePromptCible(unite, extrait, List.of());
            return ResponseEntity.ok(Map.of("prompt", prompt));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erreur", "Impossible de lire le fichier source : " + e.getMessage()));
        }
    }
}
