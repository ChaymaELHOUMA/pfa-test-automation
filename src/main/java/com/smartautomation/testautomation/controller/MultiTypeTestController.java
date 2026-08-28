package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.MultiTypeTestResult;
import com.smartautomation.testautomation.service.MultiTypeTestExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MultiTypeTestController {

    private final MultiTypeTestExecutionService executionService;
    private final com.smartautomation.testautomation.service.MultiTypeGenerationService generationService;

    public MultiTypeTestController(
            MultiTypeTestExecutionService executionService,
            com.smartautomation.testautomation.service.MultiTypeGenerationService generationService) {
        this.executionService = executionService;
        this.generationService = generationService;
    }

    @PostMapping("/executer-suite-tests")
    public ResponseEntity<MultiTypeTestResult> executerSuitePost(
            @RequestParam(required = false) String idProjet) {
        MultiTypeTestResult resultat = (idProjet == null || idProjet.isBlank() || "projet-reference".equals(idProjet))
                ? executionService.executerEtConsoliderSuite()
                : executionService.executerEtConsoliderSuite(idProjet);
        return ResponseEntity.ok(resultat);
    }

    @GetMapping("/executer-suite-tests")
    public ResponseEntity<MultiTypeTestResult> executerSuiteGet(
            @RequestParam(required = false) String idProjet) {
        MultiTypeTestResult resultat = (idProjet == null || idProjet.isBlank() || "projet-reference".equals(idProjet))
                ? executionService.executerEtConsoliderSuite()
                : executionService.executerEtConsoliderSuite(idProjet);
        return ResponseEntity.ok(resultat);
    }

    @PostMapping("/generer-suite-multi-type")
    public ResponseEntity<MultiTypeTestResult> genererSuiteMultiType(
            @RequestParam String chemin,
            @RequestParam(required = false) String idProjet) {
        MultiTypeTestResult resultat = generationService.genererSuiteMultiType(chemin, idProjet);
        return ResponseEntity.ok(resultat);
    }
}
