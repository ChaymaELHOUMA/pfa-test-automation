package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.model.*;
import com.smartautomation.testautomation.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
public class MultiTypeGenerationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AstAnalysisService astAnalysisService;
    
    @MockBean
    private CodeSnippetExtractor codeSnippetExtractor;
    
    @MockBean
    private PromptBuilderService promptBuilderService;
    
    @MockBean
    private ProjectService projectService;
    
    @MockBean
    private LlmService llmService;
    
    @MockBean
    private TestCodeExtractor testCodeExtractor;
    
    @MockBean
    private TestDeduplicationService testDeduplicationService;
    
    @MockBean
    private TestFileWriterService testFileWriterService;
    
    @MockBean
    private JestExecutionService jestExecutionService;

    @MockBean
    private MultiTypeTestExecutionService multiTypeTestExecutionService;

    @Test
    public void testGenererSuiteMultiType() throws Exception {
        // Arrange
        String chemin = "app.js";
        String idProjet = "demo-proj";

        when(projectService.resoudreCheminFichier(idProjet, chemin)).thenReturn("projet/app.js");

        UniteTestable fnUnit = new UniteTestable();
        fnUnit.setNom("calculerTotal");
        fnUnit.setType("fonction");
        
        UniteTestable routeUnit = new UniteTestable();
        routeUnit.setNom("GET /users");
        routeUnit.setType("route");
        
        AstAnalysisResult astResult = new AstAnalysisResult();
        astResult.setUnites(List.of(fnUnit, routeUnit));
        
        when(astAnalysisService.analyser(anyString()))
                .thenReturn(AstAnalysisOutcome.succes(astResult));

        ProjectService.ProjectMetadata meta = new ProjectService.ProjectMetadata(
                idProjet, "Demo Project", "app.js", "repo.js", null, List.of(), Path.of(""));
        when(projectService.getProjet(idProjet)).thenReturn(meta);

        when(codeSnippetExtractor.extraireExtrait(anyString(), anyInt(), anyInt())).thenReturn("code");
        when(promptBuilderService.construirePromptCible(any(), anyString(), anyList(), anyString())).thenReturn("prompt cible");
        when(promptBuilderService.construirePromptIntegration(any(), anyString(), any(), anyList(), anyString())).thenReturn("prompt integration");
        
        when(llmService.genererDepuisPrompt(anyString())).thenReturn("code llm");
        when(testCodeExtractor.extraireCode(anyString())).thenReturn("code extracted");
        
        TestDeduplicationResult dedupResult = new TestDeduplicationResult(1, 1, 0, List.of(), true, "code dedup");
        when(testDeduplicationService.analyserEtDedupliquer(anyString())).thenReturn(dedupResult);
        
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString(), anyString())).thenReturn(Path.of("test.js"));
        when(testFileWriterService.cheminRelatifPourJest(anyString(), anyString())).thenReturn("generated-tests/test.js");
        
        // Catégorisation simulée
        when(multiTypeTestExecutionService.determinerCategorie(anyString())).thenReturn("UNITAIRE");

        JestRunResult jestRunResult = new JestRunResult();
        jestRunResult.setSuccess(true);
        jestRunResult.setTotalTests(2);
        jestRunResult.setTestsReussis(2);
        when(jestExecutionService.executerTest(anyString())).thenReturn(ExecutionTestResult.depuisJest(jestRunResult));

        // Act & Assert
        mockMvc.perform(post("/generer-suite-multi-type")
                .param("chemin", chemin)
                .param("idProjet", idProjet)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTests").value(6)) // 2 unités, la route génère unitaire+integration (3 fichiers * 2 tests = 6)
                .andExpect(jsonPath("$.testsReussis").value(6))
                .andExpect(jsonPath("$.detailsFichiers.length()").value(3))
                // Vérifier la présence des fichiers attendus (1 unitaire pour fonction, 1 unitaire pour route, 1 intégration pour route)
                .andExpect(jsonPath("$.detailsFichiers[0].nomFichier").isNotEmpty());
    }
}
