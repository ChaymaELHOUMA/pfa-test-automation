package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.ExecutionTestResult;
import com.smartautomation.testautomation.model.JestRunResult;
import com.smartautomation.testautomation.model.NodeExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        // Par défaut, node --check réussit (pas d'erreur de syntaxe)
        when(nodeBridgeService.runScriptAtPathWithTimeout(
                eq("--check"), anyLong(), anyString()
        )).thenReturn(NodeExecutionResult.ok("", "", 0));
        jestExecutionService = new JestExecutionService(nodeBridgeService);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Tests de parserSortieJest / extraireJsonJest
    // ─────────────────────────────────────────────────────────────────────────

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
                  "numTotalTests": 0,
                  "numFailedTestSuites": 0,
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

    // ─────────────────────────────────────────────────────────────────────────
    //  Extraction JSON robuste : blocs équilibrés au milieu de logs parasites
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void extraireJsonJest_ignoreLesLogsAvantLeJson() {
        String sortieAvecLogs = "Deprecation warning: something\n" +
                "some noise {not json}\n" +
                JSON_TESTS_RACINE;
        String extrait = jestExecutionService.extraireJsonJest(sortieAvecLogs);
        assertNotNull(extrait, "Le JSON Jest doit être extrait même après des logs");
        assertTrue(extrait.contains("\"numTotalTests\""), "Le JSON extrait doit contenir la clé Jest");
    }

    @Test
    void extraireJsonJest_ignoreLesBlonsJsonSansCleCJest() {
        // Bloc JSON valide mais sans clés Jest + bloc Jest correct ensuite
        String sortie = "{\"foo\": \"bar\"}\n" + JSON_TESTS_RACINE;
        String extrait = jestExecutionService.extraireJsonJest(sortie);
        assertNotNull(extrait);
        assertTrue(extrait.contains("\"numTotalTests\""));
    }

    @Test
    void extraireJsonJest_retourneNullSiAucunBlocJest() {
        String sortie = "Juste du texte sans JSON valide\nconsole.log(\"hello\")";
        String extrait = jestExecutionService.extraireJsonJest(sortie);
        assertNull(extrait);
    }

    @Test
    void extraireJsonJest_gereAcoffadesDansLesChainesJson() {
        // Le JSON contient des accolades dans des valeurs de type chaîne
        String jsonAvecAcolladesInternes = """
                {
                  "numTotalTests": 1,
                  "numFailedTestSuites": 0,
                  "success": true,
                  "message": "erreur: Expected { recu }",
                  "testResults": []
                }
                """;
        String extrait = jestExecutionService.extraireJsonJest(jsonAvecAcolladesInternes);
        assertNotNull(extrait);
        assertTrue(extrait.contains("\"numTotalTests\""));
    }

    @Test
    void extraireBlocsGereEchappementDansChaines() {
        String texte = "{\"a\": \"valeur avec \\\" guillemet\", \"numTotalTests\": 0}";
        List<String> blocs = jestExecutionService.extraireBlocs(texte);
        assertEquals(1, blocs.size());
        assertEquals(texte, blocs.get(0));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Erreur de compilation / syntaxe JS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void executerTest_retourneErreurCompilationSiNodeCheckEchoue() {
        String stderrSyntaxe = "/path/to/test.js:3\n  const a = ;\n            ^\nSyntaxError: Unexpected token ';'";

        when(nodeBridgeService.runScriptAtPathWithTimeout(
                eq("--check"), anyLong(), anyString()
        )).thenReturn(NodeExecutionResult.echecExitCode("", stderrSyntaxe, 1));

        ExecutionTestResult resultat = jestExecutionService.executerTest("generated-tests/invalide.test.js");

        assertTrue(resultat.isErreurTechnique(), "Doit être une erreur technique");
        assertTrue(resultat.isErreurCompilation(), "Doit être categorisée comme erreur de compilation");
        assertNotNull(resultat.getMessageErreurTechnique());
        assertTrue(resultat.getMessageErreurTechnique().contains("Le test genere ne compile pas"));
        assertNotNull(resultat.getDetailErreurCompilation());
        assertTrue(resultat.getDetailErreurCompilation().contains("SyntaxError"));
    }

    @Test
    void executerTest_continueJestSiNodeCheckReussit() {
        when(nodeBridgeService.runScriptAtPathWithTimeout(
                eq("node_modules/jest/bin/jest.js"),
                anyLong(),
                eq("generated-tests/addition.test.js"),
                eq("--json"),
                eq("--ci"),
                eq("--forceExit")
        )).thenReturn(NodeExecutionResult.ok(JSON_TESTS_RACINE, "", 0));

        ExecutionTestResult resultat = jestExecutionService.executerTest("generated-tests/addition.test.js");
        assertFalse(resultat.isErreurTechnique());
        assertEquals(9, resultat.getResultatJest().getTestsReussis());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Erreur de suite Jest (compilation détectée par Jest, pas par node --check)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void enrichirComptages_compteSuitesEchoeesCommeSiEtaitUnTest() {
        // Simule un rapport Jest où la suite elle-même a échoué (SyntaxError via Babel)
        String jsonSuiteEchouee = """
                {
                  "numFailedTestSuites": 1,
                  "numRuntimeErrorTestSuites": 0,
                  "numPassedTestSuites": 0,
                  "numPassedTests": 0,
                  "numFailedTests": 0,
                  "numTotalTests": 0,
                  "success": false,
                  "testResults": [{
                    "name": "generated-tests/broken.test.js",
                    "status": "failed",
                    "message": "SyntaxError: Unexpected token",
                    "assertionResults": []
                  }]
                }
                """;

        JestRunResult resultat = jestExecutionService.parserSortieJest(jsonSuiteEchouee, "");
        assertNotNull(resultat);
        // Après enrichissement : 1 suite échouée = 1 test échoué, total = 1
        assertEquals(1, resultat.getTotalTests(), "Une suite échouée doit compter comme 1 test au total");
        assertEquals(1, resultat.getTestsEchoues(), "Une suite échouée doit compter comme 1 test échoué");
        assertEquals(0, resultat.getTestsReussis());
    }
}
