package com.smartautomation.testautomation.integration;

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
@SuppressWarnings("null")
public class GenerationTestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlmService llmService;

    @MockBean
    private TestCodeExtractor testCodeExtractor;

    @MockBean
    private TestFileWriterService testFileWriterService;

    @Test
    public void testGenererTest_Success() throws Exception {
        // Arrange
        String functionName = "add";
        String sourceCode = "function add(a, b) { return a + b; }";
        
        when(llmService.genererTest(anyString(), anyString())).thenReturn("code from llm");
        when(testCodeExtractor.extraireCode("code from llm")).thenReturn("extracted test code");
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString())).thenReturn(Path.of("test-path.test.js"));

        String requestBody = """
                {
                    "nomFonction": "%s",
                    "codeSource": "%s"
                }
                """.formatted(functionName, sourceCode);

        // Act & Assert
        mockMvc.perform(post("/generer-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.succes").value(true))
                .andExpect(jsonPath("$.cheminFichier").value("test-path.test.js"))
                .andExpect(jsonPath("$.messageErreur").isEmpty());
    }
    
    @Test
    public void testGenererTest_LlmError() throws Exception {
        // Arrange
        String functionName = "add";
        String sourceCode = "function add(a, b) { return a + b; }";
        
        when(llmService.genererTest(anyString(), anyString())).thenThrow(new com.smartautomation.testautomation.exception.LlmCallException("Service Unavailable"));

        String requestBody = """
                {
                    "nomFonction": "%s",
                    "codeSource": "%s"
                }
                """.formatted(functionName, sourceCode);

        // Act & Assert
        mockMvc.perform(post("/generer-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.succes").value(false))
                .andExpect(jsonPath("$.messageErreur").value("Erreur LLM : Service Unavailable"));
    }
}
