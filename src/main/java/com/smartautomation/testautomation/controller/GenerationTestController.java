package com.smartautomation.testautomation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.smartautomation.testautomation.model.ResultatGeneration;
import com.smartautomation.testautomation.service.GenerationTestService;

import jakarta.validation.constraints.NotBlank;


@RestController
@Validated
public class GenerationTestController {

    private final GenerationTestService generationTestService;

    public GenerationTestController(GenerationTestService generationTestService) {
        this.generationTestService = generationTestService;
    }

    @PostMapping("/generer-test")
    public ResponseEntity<ResultatGeneration> genererTest(@RequestBody @Validated GenererTestRequest requete) {
        ResultatGeneration resultat = generationTestService.genererEtSauvegarder(
                requete.nomFonction(), requete.codeSource());

        if (!resultat.isSucces()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(resultat);
        }
        return ResponseEntity.ok(resultat);
    }

    public record GenererTestRequest(
            @NotBlank(message = "nomFonction est requis") String nomFonction,
            @NotBlank(message = "codeSource est requis") String codeSource
    ) {
    }
}
