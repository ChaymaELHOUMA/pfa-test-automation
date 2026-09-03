package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.CoverageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CoverageServiceTest {

    private static final String JSON_COVERAGE_SUMMARY_EXEMPLE = """
            {
              "total": {
                "lines": { "total": 100, "covered": 80, "skipped": 0, "pct": 80.0 },
                "statements": { "total": 110, "covered": 88, "skipped": 0, "pct": 80.0 },
                "functions": { "total": 20, "covered": 15, "skipped": 0, "pct": 75.0 },
                "branches": { "total": 50, "covered": 35, "skipped": 0, "pct": 70.0 }
              },
              "d:\\\\bureau\\\\pfa-test-automation\\\\node-runner\\\\projet-reference\\\\app.js": {
                "lines": { "total": 20, "covered": 16, "skipped": 0, "pct": 80.0 },
                "statements": { "total": 20, "covered": 16, "skipped": 0, "pct": 80.0 },
                "functions": { "total": 2, "covered": 0, "skipped": 0, "pct": 0.0 },
                "branches": { "total": 4, "covered": 0, "skipped": 0, "pct": 0.0 }
              }
            }
            """;

    private NodeBridgeService nodeBridgeService;
    private CoverageService coverageService;

    @BeforeEach
    void setUp() {
        nodeBridgeService = mock(NodeBridgeService.class);
        coverageService = new CoverageService(nodeBridgeService);
    }

    @Test
    void parserRapportCouverture_extraitTotalEtDetailsFichiersCorrectement() throws IOException {
        CoverageResult result = coverageService.parserRapportCouverture(JSON_COVERAGE_SUMMARY_EXEMPLE);

        assertTrue(result.isSucces());
        assertNotNull(result.getLines());
        assertEquals(100, result.getLines().getTotal());
        assertEquals(80, result.getLines().getCovered());
        assertEquals(80.0, result.getLines().getPct());

        assertNotNull(result.getBranches());
        assertEquals(50, result.getBranches().getTotal());
        assertEquals(35, result.getBranches().getCovered());
        assertEquals(70.0, result.getBranches().getPct());

        assertNotNull(result.getDetailsParFichier());
        assertEquals(1, result.getDetailsParFichier().size());
    }

    @Test
    void parserRapportCouverture_retourneErreurSiTotalAbsent() throws IOException {
        String jsonInvalide = "{\"foo\": \"bar\"}";
        CoverageResult result = coverageService.parserRapportCouverture(jsonInvalide);

        assertFalse(result.isSucces());
        assertNotNull(result.getMessageErreur());
        assertTrue(result.getMessageErreur().contains("noeud 'total'"));
    }
}
