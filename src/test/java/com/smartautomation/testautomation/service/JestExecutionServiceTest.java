package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.ExecutionTestResult;
import com.smartautomation.testautomation.model.JestRunResult;
import com.smartautomation.testautomation.model.NodeExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JestExecutionServiceTest {

    private static final String JSON_TESTS_RACINE = """
            {
              "numFailedTestSuites": 0,
              "numFailedTests": 0,
              "numPassedTestSuites": 1,
              "numPassedTests": 9,
              "numTotalTests": 9,
              "success": true,
              "testResults": [{
                "name": "generated-tests/PUT_users_id-brut.test.js",
                "status": "passed",
                "assertionResults": [
                  {"title": "cas nominal - ok", "status": "passed", "ancestorTitles": []},
                  {"title": "cas limite - ok", "status": "passed", "ancestorTitles": []},
                  {"title": "cas invalide - ok", "status": "passed", "ancestorTitles": []},
                  {"title": "cas branche - actif", "status": "passed", "ancestorTitles": []},
                  {"title": "cas branche - inactif", "status": "passed", "ancestorTitles": []},
                  {"title": "cas branche - default", "status": "passed", "ancestorTitles": []},
                  {"title": "cas erreur - 400", "status": "passed", "ancestorTitles": []},
                  {"title": "cas limite - vide", "status": "passed", "ancestorTitles": []},
                  {"title": "cas invalide - null", "status": "passed", "ancestorTitles": []}
                ]
              }]
            }
            """;

    private NodeBridgeService nodeBridgeService;
    private JestExecutionService jestExecutionService;

    @BeforeEach
    void setUp() {
        nodeBridgeService = mock(NodeBridgeService.class);
        jestExecutionService = new JestExecutionService(nodeBridgeService);
    }

    @Test
    void parseTestsRacineSansDescribeDepuisJsonJest() {
        JestRunResult resultat = jestExecutionService.parserSortieJest(JSON_TESTS_RACINE, "");
        assertNotNull(resultat);
        assertEquals(9, resultat.getTestsReussis());
        assertEquals(9, resultat.getTotalTests());
    }

    @Test
    void parseTestsRacineViaAssertionResultsQuandCompteursAbsents() {
        String jsonSansCompteurs = """
                {
                  "success": true,
                  "testResults": [{
                    "assertionResults": [
                      {"title": "cas nominal - ok", "status": "passed", "ancestorTitles": []},
                      {"title": "cas limite - ok", "status": "passed", "ancestorTitles": []},
                      {"title": "cas erreur - ko", "status": "failed", "ancestorTitles": []}
                    ]
                  }]
                }
                """;

        JestRunResult resultat = jestExecutionService.parserSortieJest(jsonSansCompteurs, "");
        assertNotNull(resultat);
        assertEquals(2, resultat.getTestsReussis());
        assertEquals(3, resultat.getTotalTests());
    }

    @Test
    void parseJsonJestMemeApresTimeoutProcess() {
        when(nodeBridgeService.runScriptAtPathWithTimeout(
                eq("node_modules/jest/bin/jest.js"),
                anyLong(),
                eq("generated-tests/PUT_users_id-brut.test.js"),
                eq("--json"),
                eq("--ci"),
                eq("--forceExit")
        )).thenReturn(NodeExecutionResult.echecTimeout(JSON_TESTS_RACINE, "Jest forceExit"));

        ExecutionTestResult resultat = jestExecutionService.executerTest("generated-tests/PUT_users_id-brut.test.js");
        assertFalse(resultat.isErreurTechnique());
        assertEquals(9, resultat.getResultatJest().getTestsReussis());
    }
}
