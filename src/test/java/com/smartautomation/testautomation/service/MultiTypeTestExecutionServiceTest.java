package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.ExecutionTestResult;
import com.smartautomation.testautomation.model.JestRunResult;
import com.smartautomation.testautomation.model.MultiTypeTestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultiTypeTestExecutionServiceTest {

    private JestExecutionService jestExecutionService;
    private MultiTypeTestExecutionService executionService;

    @BeforeEach
    void setUp() {
        jestExecutionService = mock(JestExecutionService.class);
        executionService = new MultiTypeTestExecutionService(jestExecutionService);
    }

    @Test
    void testDeterminerCategorie() {
        assertEquals("INTEGRATION", executionService.determinerCategorie("PUT_users_id-integration.test.js"));
        assertEquals("API", executionService.determinerCategorie("GET_users-brut.test.js"));
        assertEquals("API", executionService.determinerCategorie("POST_users-structure.test.js"));
        assertEquals("API", executionService.determinerCategorie("PUT_users_id-supertest-pipeline.test.js"));
        assertEquals("UNITAIRE", executionService.determinerCategorie("addition.test.js"));
        assertEquals("UNITAIRE", executionService.determinerCategorie("validerEmail-brut.test.js"));
    }

    @Test
    void testExecuterEtConsoliderSuiteAvecResultatMock() {
        JestRunResult jestRunResult = new JestRunResult();
        jestRunResult.setSuccess(true);
        jestRunResult.setTotalTests(5);
        jestRunResult.setTestsReussis(5);
        jestRunResult.setTestsEchoues(0);

        when(jestExecutionService.executerTest(anyString()))
                .thenReturn(ExecutionTestResult.depuisJest(jestRunResult));

        MultiTypeTestResult result = executionService.executerEtConsoliderSuite();

        assertNotNull(result);
        assertTrue(result.getTotalTests() >= 0);
    }
}
