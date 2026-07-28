package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.PipelineResult;
import com.smartautomation.testautomation.service.PipelineService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/generer-et-executer-test")
    public ResponseEntity<PipelineResult> genererEtExecuter(@RequestBody @Validated PipelineRequest requete) {
        PipelineResult resultat = pipelineService.genererEcrireEtExecuter(
                requete.nomFonction(), requete.codeSource());

        if (!resultat.isSucces()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(resultat);
        }
        return ResponseEntity.ok(resultat);
    }

    public record PipelineRequest(
            @NotBlank(message = "nomFonction est requis") String nomFonction,
            @NotBlank(message = "codeSource est requis") String codeSource
    ) {
    }
}
