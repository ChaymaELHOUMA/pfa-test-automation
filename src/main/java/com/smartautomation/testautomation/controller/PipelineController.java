package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.PipelineResult;
import com.smartautomation.testautomation.service.PipelineService;
import com.smartautomation.testautomation.service.TestFileWriterService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@Validated
public class PipelineController {

    private final PipelineService pipelineService;
    private final TestFileWriterService testFileWriterService;

    public PipelineController(PipelineService pipelineService, TestFileWriterService testFileWriterService) {
        this.pipelineService = pipelineService;
        this.testFileWriterService = testFileWriterService;
    }

    @PostMapping("/generer-et-executer-test")
    public ResponseEntity<PipelineResult> genererEtExecuter(@RequestBody @Validated PipelineRequest requete) {
        String idProjet = (requete.idProjet() != null && !requete.idProjet().isBlank()) ? requete.idProjet() : "projet-reference";
        String mode = (requete.mode() != null && !requete.mode().isBlank()) ? requete.mode() : "structure";
        PipelineResult resultat = pipelineService.genererEcrireEtExecuter(
                requete.chemin(), requete.nomUnite(), mode, idProjet);

        if (!resultat.isSucces()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(resultat);
        }
        return ResponseEntity.ok(resultat);
    }

    @GetMapping("/api/fichiers-tests/contenu")
    public ResponseEntity<?> lireContenuFichierTest(
            @RequestParam String nomFichier,
            @RequestParam(required = false, defaultValue = "projet-reference") String idProjet) {
        try {
            String contenu = testFileWriterService.lireContenuFichierTest(nomFichier, idProjet);
            return ResponseEntity.ok(Map.of(
                    "nomFichier", nomFichier,
                    "contenu", contenu,
                    "idProjet", idProjet
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "erreur", e.getMessage()
            ));
        }
    }

    public record PipelineRequest(
            @NotBlank(message = "chemin est requis") String chemin,
            @NotBlank(message = "nomUnite est requis") String nomUnite,
            String mode,
            String idProjet
    ) {
    }
}
