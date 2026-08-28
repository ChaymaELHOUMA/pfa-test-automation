package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.MutationResult;
import com.smartautomation.testautomation.service.MutationTestingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mutation")
public class StrykerController {

    private final MutationTestingService mutationTestingService;

    public StrykerController(MutationTestingService mutationTestingService) {
        this.mutationTestingService = mutationTestingService;
    }

    @GetMapping("/projet-reference")
    public ResponseEntity<MutationResult> obtenirMutationProjetReference() {
        MutationResult result = mutationTestingService.executerMutationTesting("projet-reference");
        if (!result.isSucces()) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{idProjet}")
    public ResponseEntity<MutationResult> obtenirMutationProjet(@PathVariable String idProjet) {
        MutationResult result = mutationTestingService.executerMutationTesting(idProjet);
        if (!result.isSucces()) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/lancer")
    public ResponseEntity<MutationResult> lancerCampagneMutation(
            @RequestParam(required = false, defaultValue = "projet-reference") String idProjet) {
        MutationResult result = mutationTestingService.executerMutationTesting(idProjet);
        if (!result.isSucces()) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
