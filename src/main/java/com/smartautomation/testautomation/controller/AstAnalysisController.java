package com.smartautomation.testautomation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartautomation.testautomation.model.AstAnalysisOutcome;
import com.smartautomation.testautomation.service.AstAnalysisService;
import com.smartautomation.testautomation.service.ProjectService;

@RestController
public class AstAnalysisController {

    private final AstAnalysisService astAnalysisService;
    private final ProjectService projectService;

    public AstAnalysisController(AstAnalysisService astAnalysisService, ProjectService projectService) {
        this.astAnalysisService = astAnalysisService;
        this.projectService = projectService;
    }

    @GetMapping("/analyser-ast")
    public ResponseEntity<AstAnalysisOutcome> analyser(
            @RequestParam String chemin,
            @RequestParam(required = false, defaultValue = "projet-reference") String idProjet) {

        String cheminResolu = projectService.resoudreCheminFichier(idProjet, chemin);
        AstAnalysisOutcome resultat = astAnalysisService.analyser(cheminResolu);

        if (resultat.isErreurTechnique()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(resultat);
        }
        return ResponseEntity.ok(resultat);
    }
}
