package com.smartautomation.testautomation.integration;

import com.smartautomation.testautomation.model.ExecutionTestResult;
import com.smartautomation.testautomation.model.JestRunResult;
import com.smartautomation.testautomation.service.JestExecutionService;
import com.smartautomation.testautomation.service.LlmService;
import com.smartautomation.testautomation.service.TestCodeExtractor;
import com.smartautomation.testautomation.service.TestFileWriterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PipelineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlmService llmService;

    @MockBean
    private TestCodeExtractor testCodeExtractor;

    @MockBean
    private TestFileWriterService testFileWriterService;

    @MockBean
    private JestExecutionService jestExecutionService;

    @Test
    public void testGenererEtExecuter_Success() throws Exception {
        // Arrange
        String functionName = "multiply";
        String sourceCode = "function multiply(a, b) { return a * b; }";

        when(llmService.genererTest(anyString(), anyString())).thenReturn("code from llm");
        when(testCodeExtractor.extraireCode("code from llm")).thenReturn("extracted test code");
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString())).thenReturn(Path.of("test-path.test.js"));
        when(testFileWriterService.cheminRelatifPourJest(anyString())).thenReturn("test-path.test.js");
        
        JestRunResult jestRunResult = new JestRunResult();
        jestRunResult.setSuccess(true);
        jestRunResult.setTotalTests(5);
        jestRunResult.setTestsReussis(5);
        jestRunResult.setTestsEchoues(0);
        ExecutionTestResult executionResult = ExecutionTestResult.depuisJest(jestRunResult);
        when(jestExecutionService.executerTest(anyString())).thenReturn(executionResult);

        String requestBody = """
                {
                    "nomFonction": "%s",
                    "codeSource": "%s"
                }
                """.formatted(functionName, sourceCode);

        // Act & Assert
        mockMvc.perform(post("/generer-et-executer-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.succes").value(true))
                .andExpect(jsonPath("$.cheminFichierTest").value("test-path.test.js"))
                .andExpect(jsonPath("$.messageErreur").isEmpty())
                .andExpect(jsonPath("$.totalTests").value(5))
                .andExpect(jsonPath("$.testsReussis").value(5))
                .andExpect(jsonPath("$.testsEchoues").value(0));
    }
    
    @Test
    public void testGenererEtExecuter_ExecutionError() throws Exception {
        // Arrange
        String functionName = "multiply";
        String sourceCode = "function multiply(a, b) { return a * b; }";

        when(llmService.genererTest(anyString(), anyString())).thenReturn("code from llm");
        when(testCodeExtractor.extraireCode("code from llm")).thenReturn("extracted test code");
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString())).thenReturn(Path.of("test-path.test.js"));
        when(testFileWriterService.cheminRelatifPourJest(anyString())).thenReturn("test-path.test.js");
        
        ExecutionTestResult executionResult = ExecutionTestResult.erreurTechnique("Jest execution failed");
        when(jestExecutionService.executerTest(anyString())).thenReturn(executionResult);

        String requestBody = """
                {
                    "nomFonction": "%s",
                    "codeSource": "%s"
                }
                """.formatted(functionName, sourceCode);

        // Act & Assert
        mockMvc.perform(post("/generer-et-executer-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.succes").value(false))
                .andExpect(jsonPath("$.cheminFichierTest").value("test-path.test.js"))
                .andExpect(jsonPath("$.messageErreur").value("Jest execution failed"));
    }
}
