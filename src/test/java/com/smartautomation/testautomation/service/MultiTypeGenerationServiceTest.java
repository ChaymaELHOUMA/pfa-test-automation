package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.*;
import com.smartautomation.testautomation.model.MultiTypeTestResult.FichierDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MultiTypeGenerationServiceTest {

    @Mock
    private AstAnalysisService astAnalysisService;
    @Mock
    private CodeSnippetExtractor codeSnippetExtractor;
    @Mock
    private PromptBuilderService promptBuilderService;
    @Mock
    private ProjectService projectService;
    @Mock
    private LlmService llmService;
    @Mock
    private TestCodeExtractor testCodeExtractor;
    @Mock
    private TestDeduplicationService testDeduplicationService;
    @Mock
    private TestFileWriterService testFileWriterService;
    @Mock
    private JestExecutionService jestExecutionService;
    @Mock
    private MultiTypeTestExecutionService multiTypeTestExecutionService;

    @InjectMocks
    private MultiTypeGenerationService generationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenererSuiteMultiType_FonctionUnitaireSeulement() throws Exception {
        // Arrange
        when(projectService.resoudreCheminFichier(anyString(), anyString())).thenReturn("projet/app.js");
        
        UniteTestable unite = new UniteTestable();
        unite.setNom("calculerTotal");
        unite.setType("fonction");
        
        AstAnalysisResult astResult = new AstAnalysisResult();
        astResult.setUnites(List.of(unite));
        when(astAnalysisService.analyser(anyString())).thenReturn(AstAnalysisOutcome.succes(astResult));
        
        when(codeSnippetExtractor.extraireExtrait(anyString(), anyInt(), anyInt())).thenReturn("function calculerTotal() {}");
        when(promptBuilderService.construirePromptCible(any(), anyString(), anyList(), anyString())).thenReturn("prompt unit");
        
        when(llmService.genererDepuisPrompt("prompt unit")).thenReturn("code unit");
        when(testCodeExtractor.extraireCode("code unit")).thenReturn("code unit extracted");
        
        TestDeduplicationResult dedupResult = new TestDeduplicationResult(1, 1, 0, List.of(), true, "code unit extracted");
        when(testDeduplicationService.analyserEtDedupliquer(anyString())).thenReturn(dedupResult);
        
        when(testFileWriterService.ecrireFichierTest(eq("calculerTotal-unitaire"), anyString(), anyString()))
            .thenReturn(Path.of("calculerTotal-unitaire.test.js"));
            
        when(multiTypeTestExecutionService.determinerCategorie(anyString())).thenReturn("UNITAIRE");
        when(testFileWriterService.cheminRelatifPourJest(anyString(), anyString())).thenReturn("chemin");
        
        JestRunResult jestRunResult = new JestRunResult();
        jestRunResult.setSuccess(true);
        jestRunResult.setTotalTests(2);
        jestRunResult.setTestsReussis(2);
        when(jestExecutionService.executerTest(anyString())).thenReturn(ExecutionTestResult.depuisJest(jestRunResult));

        // Act
        MultiTypeTestResult result = generationService.genererSuiteMultiType("app.js", "demo-proj");

        // Assert
        assertEquals(2, result.getTotalTests());
        assertEquals(2, result.getTestsReussis());
        assertEquals(1, result.getDetailsFichiers().size());
        
        FichierDetail detail = result.getDetailsFichiers().get(0);
        assertEquals("calculerTotal-unitaire.test.js", detail.nomFichier());
        
        // Verifie qu'un seul prompt a ete construit (pas le prompt integration)
        verify(promptBuilderService, times(1)).construirePromptCible(any(), anyString(), anyList(), anyString());
        verify(promptBuilderService, never()).construirePromptIntegration(any(), anyString(), anyString(), anyList(), anyString());
    }

    @Test
    void testGenererSuiteMultiType_RouteAvecRepository_DeuxTypes() throws Exception {
        // Arrange
        when(projectService.resoudreCheminFichier(anyString(), anyString())).thenReturn("projet/routes.js");
        
        UniteTestable unite = new UniteTestable();
        unite.setNom("GET /users");
        unite.setType("route");
        
        AstAnalysisResult astResult = new AstAnalysisResult();
        astResult.setUnites(List.of(unite));
        when(astAnalysisService.analyser(anyString())).thenReturn(AstAnalysisOutcome.succes(astResult));
        
        // Projet avec repository valid
        ProjectService.ProjectMetadata meta = new ProjectService.ProjectMetadata("demo-proj", "Demo", "app.js", "repo.js", null, List.of(), Path.of(""));
        when(projectService.getProjet("demo-proj")).thenReturn(meta);
        when(projectService.construireContexteCouches(anyString(), anyString())).thenReturn("contexte");

        when(codeSnippetExtractor.extraireExtrait(anyString(), anyInt(), anyInt())).thenReturn("route()");
        when(promptBuilderService.construirePromptCible(any(), anyString(), anyList(), anyString())).thenReturn("prompt unit");
        when(promptBuilderService.construirePromptIntegration(any(), anyString(), anyString(), anyList(), anyString())).thenReturn("prompt int");
        
        when(llmService.genererDepuisPrompt(anyString())).thenReturn("code generated");
        when(testCodeExtractor.extraireCode(anyString())).thenReturn("code");
        TestDeduplicationResult dedupResult = new TestDeduplicationResult(1, 1, 0, List.of(), true, "code dedup");
        when(testDeduplicationService.analyserEtDedupliquer(anyString())).thenReturn(dedupResult);
        
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString(), anyString()))
            .thenReturn(Path.of("test.js"));
            
        when(testFileWriterService.cheminRelatifPourJest(anyString(), anyString())).thenReturn("chemin");
        when(multiTypeTestExecutionService.determinerCategorie(anyString())).thenReturn("API");
        
        JestRunResult jestRunResult = new JestRunResult();
        jestRunResult.setSuccess(true);
        jestRunResult.setTotalTests(1);
        jestRunResult.setTestsReussis(1);
        when(jestExecutionService.executerTest(anyString())).thenReturn(ExecutionTestResult.depuisJest(jestRunResult));

        // Act
        MultiTypeTestResult result = generationService.genererSuiteMultiType("routes.js", "demo-proj");

        // Assert
        assertEquals(2, result.getDetailsFichiers().size());
        assertEquals(2, result.getTotalTests()); // 1 unit + 1 int
        
        verify(promptBuilderService, times(1)).construirePromptCible(any(), anyString(), anyList(), anyString());
        verify(promptBuilderService, times(1)).construirePromptIntegration(any(), anyString(), anyString(), anyList(), anyString());
    }

    @Test
    void testGenererSuiteMultiType_RouteSansRepository_UnType() throws Exception {
        // Arrange
        when(projectService.resoudreCheminFichier(anyString(), anyString())).thenReturn("projet/routes.js");
        
        UniteTestable unite = new UniteTestable();
        unite.setNom("POST /users");
        unite.setType("route");
        
        AstAnalysisResult astResult = new AstAnalysisResult();
        astResult.setUnites(List.of(unite));
        when(astAnalysisService.analyser(anyString())).thenReturn(AstAnalysisOutcome.succes(astResult));
        
        // Projet sans repo ni pointEntree
        ProjectService.ProjectMetadata meta = new ProjectService.ProjectMetadata("demo-proj", "Demo", null, null, null, List.of(), Path.of(""));
        when(projectService.getProjet("demo-proj")).thenReturn(meta);

        when(codeSnippetExtractor.extraireExtrait(anyString(), anyInt(), anyInt())).thenReturn("route()");
        when(promptBuilderService.construirePromptCible(any(), anyString(), anyList(), anyString())).thenReturn("prompt unit");
        
        when(llmService.genererDepuisPrompt(anyString())).thenReturn("code");
        when(testCodeExtractor.extraireCode(anyString())).thenReturn("code");
        when(testDeduplicationService.analyserEtDedupliquer(anyString())).thenReturn(new TestDeduplicationResult(1, 1, 0, List.of(), true, "code dedup"));
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString(), anyString())).thenReturn(Path.of("test.js"));
        when(testFileWriterService.cheminRelatifPourJest(anyString(), anyString())).thenReturn("chemin");
        when(jestExecutionService.executerTest(anyString())).thenReturn(ExecutionTestResult.erreurTechnique("error"));

        // Act
        MultiTypeTestResult result = generationService.genererSuiteMultiType("routes.js", "demo-proj");

        // Assert
        assertEquals(1, result.getDetailsFichiers().size());
        
        // Verifie pas de mode integration
        verify(promptBuilderService, times(1)).construirePromptCible(any(), anyString(), anyList(), anyString());
        verify(promptBuilderService, never()).construirePromptIntegration(any(), anyString(), anyString(), anyList(), anyString());
    }

    @Test
    void testGenererSuiteMultiType_EchecPartielContinue() throws Exception {
        when(projectService.resoudreCheminFichier(anyString(), anyString())).thenReturn("projet/app.js");
        
        UniteTestable u1 = new UniteTestable(); u1.setNom("u1"); u1.setType("fonction");
        UniteTestable u2 = new UniteTestable(); u2.setNom("u2"); u2.setType("fonction");
        
        AstAnalysisResult astResult = new AstAnalysisResult();
        astResult.setUnites(List.of(u1, u2));
        when(astAnalysisService.analyser(anyString())).thenReturn(AstAnalysisOutcome.succes(astResult));
        
        when(codeSnippetExtractor.extraireExtrait(anyString(), anyInt(), anyInt())).thenReturn("code");
        
        // U1 echoue sur le prompt
        when(promptBuilderService.construirePromptCible(eq(u1), anyString(), anyList(), anyString())).thenReturn(null);
        
        // U2 reussit
        when(promptBuilderService.construirePromptCible(eq(u2), anyString(), anyList(), anyString())).thenReturn("prompt u2");
        when(llmService.genererDepuisPrompt("prompt u2")).thenReturn("code generated 2");
        when(testCodeExtractor.extraireCode(anyString())).thenReturn("code 2");
        when(testDeduplicationService.analyserEtDedupliquer(anyString())).thenReturn(new TestDeduplicationResult(1, 1, 0, List.of(), true, "code dedup 2"));
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString(), anyString())).thenReturn(Path.of("u2.test.js"));
        when(testFileWriterService.cheminRelatifPourJest(anyString(), anyString())).thenReturn("chemin");
        when(multiTypeTestExecutionService.determinerCategorie(anyString())).thenReturn("UNITAIRE");
        
        JestRunResult jestRunResult = new JestRunResult();
        jestRunResult.setSuccess(true);
        jestRunResult.setTotalTests(3);
        jestRunResult.setTestsReussis(3);
        when(jestExecutionService.executerTest(anyString())).thenReturn(ExecutionTestResult.depuisJest(jestRunResult));

        MultiTypeTestResult result = generationService.genererSuiteMultiType("app.js", "demo-proj");
        
        // Verifie que le detail de u1 indique GENERATION en erreur, et u2 reussit
        assertEquals(2, result.getDetailsFichiers().size());
        assertEquals("GENERATION", result.getDetailsFichiers().get(0).categorie());
        assertEquals(3, result.getTotalTests()); // Seulement U2 compté
    }
}
