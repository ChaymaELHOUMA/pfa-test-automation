package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.MutantMetrics;
import com.smartautomation.testautomation.model.MutationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MutationTestingServiceTest {

    private static final String JSON_STRYKER_RAPPORT_EXEMPLE = """
            {
              "schemaVersion": "1",
              "files": {
                "projet-reference/routes-utilisateurs.js": {
                  "language": "javascript",
                  "source": "const express = require('express');",
                  "mutants": [
                    { "id": "1", "mutatorName": "ArithmeticOperator", "status": "Killed" },
                    { "id": "2", "mutatorName": "StringLiteral", "status": "Killed" },
                    { "id": "3", "mutatorName": "EqualityOperator", "status": "Survived" },
                    { "id": "4", "mutatorName": "ConditionalExpression", "status": "NoCoverage" },
                    { "id": "5", "mutatorName": "BlockStatement", "status": "TimedOut" }
                  ]
                }
              }
            }
            """;

    private NodeBridgeService nodeBridgeService;
    private MutationTestingService mutationTestingService;

    @BeforeEach
    void setUp() {
        nodeBridgeService = mock(NodeBridgeService.class);
        mutationTestingService = new MutationTestingService(nodeBridgeService);
    }

    @Test
    void parserRapportStryker_calculeScoreEtMetriquesCorrectement() throws IOException {
        MutationResult result = mutationTestingService.parserRapportStryker(JSON_STRYKER_RAPPORT_EXEMPLE);

        assertTrue(result.isSucces());
        assertNotNull(result.getMetrics());

        MutantMetrics metrics = result.getMetrics();
        assertEquals(2, metrics.getKilled());
        assertEquals(1, metrics.getSurvived());
        assertEquals(1, metrics.getTimedOut());
        assertEquals(1, metrics.getNoCoverage());
        assertEquals(5, metrics.getTotal());

        // (2 Killed + 1 TimedOut) / (2 Killed + 1 Survived + 1 TimedOut + 1 NoCoverage) = 3 / 5 = 60.0%
        assertEquals(60.0, metrics.getScoreMutationPct());

        assertNotNull(result.getMetricsParFichier());
        assertTrue(result.getMetricsParFichier().containsKey("projet-reference/routes-utilisateurs.js"));
    }

    @Test
    void parserRapportStryker_retourneErreurSiFilesAbsent() throws IOException {
        String jsonInvalide = "{\"schemaVersion\": \"1\"}";
        MutationResult result = mutationTestingService.parserRapportStryker(jsonInvalide);

        assertFalse(result.isSucces());
        assertNotNull(result.getMessageErreur());
        assertTrue(result.getMessageErreur().contains("clé 'files'"));
    }
}
