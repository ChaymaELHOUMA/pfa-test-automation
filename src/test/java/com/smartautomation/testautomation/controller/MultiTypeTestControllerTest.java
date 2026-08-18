package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.MultiTypeTestResult;
import com.smartautomation.testautomation.service.LlmService;
import com.smartautomation.testautomation.service.MultiTypeTestExecutionService;
import com.smartautomation.testautomation.service.NodeBridgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MultiTypeTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MultiTypeTestExecutionService executionService;

    @MockBean
    private LlmService llmService;

    @MockBean
    private NodeBridgeService nodeBridgeService;

    @Test
    public void testExecuterSuiteGet() throws Exception {
        MultiTypeTestResult.CategorieMetrics unitaires = new MultiTypeTestResult.CategorieMetrics(5, 5, 0, 100.0);
        MultiTypeTestResult.CategorieMetrics integration = new MultiTypeTestResult.CategorieMetrics(2, 2, 0, 100.0);
        MultiTypeTestResult.CategorieMetrics api = new MultiTypeTestResult.CategorieMetrics(10, 10, 0, 100.0);

        MultiTypeTestResult mockResult = new MultiTypeTestResult(
                17, 17, 0, 100.0,
                Map.of("UNITAIRE", unitaires, "INTEGRATION", integration, "API", api),
                List.of(new MultiTypeTestResult.FichierDetail("addition.test.js", "UNITAIRE", 5, 5, 0, true, null))
        );

        when(executionService.executerEtConsoliderSuite()).thenReturn(mockResult);

        mockMvc.perform(get("/executer-suite-tests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTests").value(17))
                .andExpect(jsonPath("$.testsReussis").value(17))
                .andExpect(jsonPath("$.testsEchoues").value(0))
                .andExpect(jsonPath("$.tauxSucces").value(100.0))
                .andExpect(jsonPath("$.resultatsParType.UNITAIRE.total").value(5))
                .andExpect(jsonPath("$.resultatsParType.INTEGRATION.total").value(2))
                .andExpect(jsonPath("$.resultatsParType.API.total").value(10));
    }

    @Test
    public void testExecuterSuitePost() throws Exception {
        MultiTypeTestResult mockResult = new MultiTypeTestResult(
                10, 10, 0, 100.0,
                Map.of(),
                List.of()
        );

        when(executionService.executerEtConsoliderSuite()).thenReturn(mockResult);

        mockMvc.perform(post("/executer-suite-tests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTests").value(10));
    }
}
