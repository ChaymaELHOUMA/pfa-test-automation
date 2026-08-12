package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.PromptComparisonResult;
import com.smartautomation.testautomation.service.PromptComparisonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class PromptComparisonController {

    private final PromptComparisonService promptComparisonService;

    public PromptComparisonController(PromptComparisonService promptComparisonService) {
        this.promptComparisonService = promptComparisonService;
    }

    @GetMapping("/comparer-prompts")
    public ResponseEntity<?> comparer(@RequestParam String chemin, @RequestParam String nomUnite) {
        try {
            PromptComparisonResult resultat = promptComparisonService.comparer(chemin, nomUnite);
            return ResponseEntity.ok(resultat);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("erreur", e.getMessage()));
        }
    }
}
