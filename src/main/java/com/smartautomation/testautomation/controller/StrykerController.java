package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.MutationResult;
import com.smartautomation.testautomation.service.MutationTestingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mutation")
public class StrykerController {

    private final MutationTestingService mutationTestingService;

    public StrykerController(MutationTestingService mutationTestingService) {
        this.mutationTestingService = mutationTestingService;
    }

    @GetMapping("/projet-reference")
    public ResponseEntity<MutationResult> obtenirMutationProjetReference() {
        MutationResult result = mutationTestingService.executerMutationTesting();
        if (!result.isSucces()) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/lancer")
    public ResponseEntity<MutationResult> lancerCampagneMutation() {
        MutationResult result = mutationTestingService.executerMutationTesting();
        if (!result.isSucces()) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
