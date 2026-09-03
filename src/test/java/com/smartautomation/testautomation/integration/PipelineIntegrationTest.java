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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
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

    @MockBean
    private com.smartautomation.testautomation.service.AstAnalysisService astAnalysisService;

    @MockBean
    private com.smartautomation.testautomation.service.CodeSnippetExtractor codeSnippetExtractor;

    @MockBean
    private com.smartautomation.testautomation.service.PromptBuilderService promptBuilderService;

    @MockBean
    private com.smartautomation.testautomation.service.ProjectService projectService;

    @Test
    public void testGenererEtExecuter_Success() throws Exception {
        // Arrange
        String chemin = "routes-utilisateurs.js";
        String nomUnite = "validerEmail";

        // ProjectService résout le chemin
        when(projectService.resoudreCheminFichier(anyString(), anyString()))
                .thenReturn("projet-reference/routes-utilisateurs.js");

        // AstAnalysisService retourne une unité testable
        com.smartautomation.testautomation.model.UniteTestable unite =
                new com.smartautomation.testautomation.model.UniteTestable();
        unite.setNom(nomUnite);
        unite.setType("fonction");
        unite.setLigneDebut(6);
        unite.setLigneFin(9);

        com.smartautomation.testautomation.model.AstAnalysisResult astResult =
                new com.smartautomation.testautomation.model.AstAnalysisResult();
        astResult.setUnites(java.util.List.of(unite));

        com.smartautomation.testautomation.model.AstAnalysisOutcome outcome =
                com.smartautomation.testautomation.model.AstAnalysisOutcome.succes(astResult);
        when(astAnalysisService.analyser(anyString())).thenReturn(outcome);

        // CodeSnippetExtractor retourne le vrai code
        when(codeSnippetExtractor.extraireExtrait(anyString(), anyInt(), anyInt()))
                .thenReturn("function validerEmail(email) { return email.includes('@'); }");

        // PromptBuilderService construit le prompt structuré
        when(promptBuilderService.construirePromptCible(
                org.mockito.ArgumentMatchers.any(),
                anyString(),
                org.mockito.ArgumentMatchers.anyList(),
                anyString()))
                .thenReturn("prompt structure mock");

        // LLM retourne du code (via genererDepuisPrompt, PAS genererTest)
        when(llmService.genererDepuisPrompt(anyString())).thenReturn("```javascript\nextracted test code\n```");
        when(testCodeExtractor.extraireCode(anyString())).thenReturn("extracted test code");
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString(), anyString()))
                .thenReturn(Path.of("test-path.test.js"));
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString()))
                .thenReturn(Path.of("test-path.test.js"));
        when(testFileWriterService.cheminRelatifPourJest(anyString(), anyString()))
                .thenReturn("generated-tests/validerEmail.test.js");
        when(testFileWriterService.cheminRelatifPourJest(anyString()))
                .thenReturn("generated-tests/validerEmail.test.js");

        JestRunResult jestRunResult = new JestRunResult();
        jestRunResult.setSuccess(true);
        jestRunResult.setTotalTests(5);
        jestRunResult.setTestsReussis(5);
        jestRunResult.setTestsEchoues(0);
        ExecutionTestResult executionResult = ExecutionTestResult.depuisJest(jestRunResult);
        when(jestExecutionService.executerTest(anyString())).thenReturn(executionResult);

        String requestBody = """
                {
                    "chemin": "%s",
                    "nomUnite": "%s",
                    "mode": "structure"
                }
                """.formatted(chemin, nomUnite);

        // Act & Assert
        mockMvc.perform(post("/generer-et-executer-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.succes").value(true))
                .andExpect(jsonPath("$.messageErreur").isEmpty())
                .andExpect(jsonPath("$.totalTests").value(5))
                .andExpect(jsonPath("$.testsReussis").value(5))
                .andExpect(jsonPath("$.testsEchoues").value(0));
    }

    @Test
    public void testGenererEtExecuter_UnitIntrouvable() throws Exception {
        // Arrange : l'AST réussit mais l'unité demandée n'existe pas dans le fichier
        when(projectService.resoudreCheminFichier(anyString(), anyString()))
                .thenReturn("projet-reference/routes-utilisateurs.js");

        com.smartautomation.testautomation.model.AstAnalysisResult astResult =
                new com.smartautomation.testautomation.model.AstAnalysisResult();
        astResult.setUnites(java.util.List.of()); // aucune unité

        com.smartautomation.testautomation.model.AstAnalysisOutcome outcome =
                com.smartautomation.testautomation.model.AstAnalysisOutcome.succes(astResult);
        when(astAnalysisService.analyser(anyString())).thenReturn(outcome);

        String requestBody = """
                {
                    "chemin": "routes-utilisateurs.js",
                    "nomUnite": "fonctionInexistante"
                }
                """;

        // Act & Assert : 422 avec message d'erreur explicite contenant le nom de l'unité
        mockMvc.perform(post("/generer-et-executer-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.succes").value(false))
                .andExpect(jsonPath("$.messageErreur").value(
                        org.hamcrest.Matchers.containsString("fonctionInexistante")));
    }

    @Test
    public void testGenererEtExecuter_ExecutionError() throws Exception {
        // Arrange
        when(projectService.resoudreCheminFichier(anyString(), anyString()))
                .thenReturn("projet-reference/routes-utilisateurs.js");

        com.smartautomation.testautomation.model.UniteTestable unite =
                new com.smartautomation.testautomation.model.UniteTestable();
        unite.setNom("validerEmail");
        unite.setType("fonction");
        unite.setLigneDebut(6);
        unite.setLigneFin(9);

        com.smartautomation.testautomation.model.AstAnalysisResult astResult =
                new com.smartautomation.testautomation.model.AstAnalysisResult();
        astResult.setUnites(java.util.List.of(unite));

        when(astAnalysisService.analyser(anyString()))
                .thenReturn(com.smartautomation.testautomation.model.AstAnalysisOutcome.succes(astResult));
        when(codeSnippetExtractor.extraireExtrait(anyString(), anyInt(), anyInt()))
                .thenReturn("function validerEmail(email) { return email.includes('@'); }");
        when(promptBuilderService.construirePromptCible(
                org.mockito.ArgumentMatchers.any(),
                anyString(),
                org.mockito.ArgumentMatchers.anyList(),
                anyString()))
                .thenReturn("prompt structure mock");
        when(llmService.genererDepuisPrompt(anyString())).thenReturn("```javascript\ntest code\n```");
        when(testCodeExtractor.extraireCode(anyString())).thenReturn("test code");
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString()))
                .thenReturn(Path.of("test-path.test.js"));
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString(), anyString()))
                .thenReturn(Path.of("test-path.test.js"));
        when(testFileWriterService.cheminRelatifPourJest(anyString()))
                .thenReturn("generated-tests/validerEmail.test.js");
        when(testFileWriterService.cheminRelatifPourJest(anyString(), anyString()))
                .thenReturn("generated-tests/validerEmail.test.js");

        ExecutionTestResult executionResult = ExecutionTestResult.erreurTechnique("Jest execution failed");
        when(jestExecutionService.executerTest(anyString())).thenReturn(executionResult);

        String requestBody = """
                {
                    "chemin": "routes-utilisateurs.js",
                    "nomUnite": "validerEmail",
                    "mode": "structure"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/generer-et-executer-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.succes").value(false))
                .andExpect(jsonPath("$.messageErreur").value("Jest execution failed"));
    }

    @Test
    public void testGenererEtExecuter_IntegrationMode() throws Exception {
        // Arrange
        String chemin = "routes-utilisateurs.js";
        String nomUnite = "POST /users";

        when(projectService.resoudreCheminFichier(anyString(), anyString()))
                .thenReturn("projet-reference/routes-utilisateurs.js");

        com.smartautomation.testautomation.model.UniteTestable unite =
                new com.smartautomation.testautomation.model.UniteTestable();
        unite.setNom(nomUnite);
        unite.setType("route");
        unite.setLigneDebut(10);
        unite.setLigneFin(20);

        com.smartautomation.testautomation.model.AstAnalysisResult astResult =
                new com.smartautomation.testautomation.model.AstAnalysisResult();
        astResult.setUnites(java.util.List.of(unite));

        when(astAnalysisService.analyser(anyString()))
                .thenReturn(com.smartautomation.testautomation.model.AstAnalysisOutcome.succes(astResult));

        when(codeSnippetExtractor.extraireExtrait(anyString(), anyInt(), anyInt()))
                .thenReturn("app.post('/users', ...)");

        String expectedContext = "--- Repository (myRepo.js) ---\nrepo content\n\n";
        when(projectService.construireContexteCouches(anyString(), anyString()))
                .thenReturn(expectedContext);

        when(promptBuilderService.construirePromptIntegration(
                org.mockito.ArgumentMatchers.any(),
                anyString(),
                org.mockito.ArgumentMatchers.eq(expectedContext),
                org.mockito.ArgumentMatchers.anyList(),
                anyString()))
                .thenReturn("prompt integration mock");

        when(llmService.genererDepuisPrompt(anyString())).thenReturn("```javascript\nintegration test code\n```");
        when(testCodeExtractor.extraireCode(anyString())).thenReturn("integration test code");
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString()))
                .thenReturn(Path.of("test-path.test.js"));
        when(testFileWriterService.ecrireFichierTest(anyString(), anyString(), anyString()))
                .thenReturn(Path.of("test-path.test.js"));
        when(testFileWriterService.cheminRelatifPourJest(anyString()))
                .thenReturn("generated-tests/POST_users.test.js");
        when(testFileWriterService.cheminRelatifPourJest(anyString(), anyString()))
                .thenReturn("generated-tests/POST_users.test.js");

        JestRunResult jestRunResult = new JestRunResult();
        jestRunResult.setSuccess(true);
        jestRunResult.setTotalTests(2);
        jestRunResult.setTestsReussis(2);
        jestRunResult.setTestsEchoues(0);
        ExecutionTestResult executionResult = ExecutionTestResult.depuisJest(jestRunResult);
        when(jestExecutionService.executerTest(anyString())).thenReturn(executionResult);

        String requestBody = """
                {
                    "chemin": "%s",
                    "nomUnite": "%s",
                    "mode": "integration"
                }
                """.formatted(chemin, nomUnite);

        // Act & Assert
        mockMvc.perform(post("/generer-et-executer-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.succes").value(true));
                
        // Validation que la bonne méthode a été appelée avec le contexte fourni par ProjectService
        org.mockito.Mockito.verify(promptBuilderService).construirePromptIntegration(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(expectedContext),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }
}
