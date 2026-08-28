package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.CoverageResult;
import com.smartautomation.testautomation.service.CoverageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coverage")
public class CoverageController {

    private final CoverageService coverageService;

    public CoverageController(CoverageService coverageService) {
        this.coverageService = coverageService;
    }

    @GetMapping("/projet-reference")
    public ResponseEntity<CoverageResult> mesurerCouvertureProjetReference() {
        CoverageResult result = coverageService.mesurerCouverturePourProjet("projet-reference");
        if (!result.isSucces()) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{idProjet}")
    public ResponseEntity<CoverageResult> mesurerCouvertureParProjet(@PathVariable String idProjet) {
        CoverageResult result = coverageService.mesurerCouverturePourProjet(idProjet);
        if (!result.isSucces()) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/mesurer")
    public ResponseEntity<CoverageResult> mesurerCouvertureTestsSpecifiques(
            @RequestParam(required = false, defaultValue = "projet-reference") String idProjet,
            @RequestBody(required = false) List<String> testFiles) {
        String[] files = (testFiles != null && !testFiles.isEmpty())
                ? testFiles.toArray(new String[0])
                : new String[0];
        CoverageResult result = coverageService.mesurerCouverturePourProjet(idProjet, files);
        if (!result.isSucces()) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
