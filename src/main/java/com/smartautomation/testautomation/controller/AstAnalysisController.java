package com.smartautomation.testautomation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartautomation.testautomation.model.AstAnalysisOutcome;
import com.smartautomation.testautomation.service.AstAnalysisService;

@RestController
public class AstAnalysisController {

    private final AstAnalysisService astAnalysisService;

    public AstAnalysisController(AstAnalysisService astAnalysisService) {
        this.astAnalysisService = astAnalysisService;
    }

   
    @GetMapping("/analyser-ast")
    public ResponseEntity<AstAnalysisOutcome> analyser(@RequestParam String chemin) {
        AstAnalysisOutcome resultat = astAnalysisService.analyser(chemin);

        if (resultat.isErreurTechnique()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(resultat);
        }
        return ResponseEntity.ok(resultat);
    }
}
