package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.TestDeduplicationResult;
import com.smartautomation.testautomation.service.TestDeduplicationService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Validated
public class TestDeduplicationController {

    private final TestDeduplicationService deduplicationService;

    public TestDeduplicationController(TestDeduplicationService deduplicationService) {
        this.deduplicationService = deduplicationService;
    }

    @PostMapping("/valider-non-redondance")
    public ResponseEntity<TestDeduplicationResult> validerNonRedondance(@RequestBody @Validated AnalyseCodeRequest requete) {
        TestDeduplicationResult resultat = deduplicationService.analyserEtDedupliquer(requete.codeTest());
        return ResponseEntity.ok(resultat);
    }

    @PostMapping("/extraire-titres-tests")
    public ResponseEntity<Map<String, List<String>>> extraireTitresTests(@RequestBody @Validated AnalyseCodeRequest requete) {
        List<String> titres = deduplicationService.extraireTitresTests(requete.codeTest());
        return ResponseEntity.ok(Map.of("titres", titres));
    }

    public record AnalyseCodeRequest(
            @NotBlank(message = "codeTest est requis") String codeTest
    ) {
    }
}
