package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.MultiTypeTestResult;
import com.smartautomation.testautomation.service.MultiTypeTestExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MultiTypeTestController {

    private final MultiTypeTestExecutionService executionService;

    public MultiTypeTestController(MultiTypeTestExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/executer-suite-tests")
    public ResponseEntity<MultiTypeTestResult> executerSuitePost() {
        MultiTypeTestResult resultat = executionService.executerEtConsoliderSuite();
        return ResponseEntity.ok(resultat);
    }

    @GetMapping("/executer-suite-tests")
    public ResponseEntity<MultiTypeTestResult> executerSuiteGet() {
        MultiTypeTestResult resultat = executionService.executerEtConsoliderSuite();
        return ResponseEntity.ok(resultat);
    }
}
