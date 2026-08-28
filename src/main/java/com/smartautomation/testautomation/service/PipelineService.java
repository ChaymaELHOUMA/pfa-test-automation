package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.exception.LlmCallException;
import com.smartautomation.testautomation.exception.TestGenerationException;
import com.smartautomation.testautomation.model.ExecutionTestResult;
import com.smartautomation.testautomation.model.JestRunResult;
import com.smartautomation.testautomation.model.PipelineResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class PipelineService {

    private static final Logger log = LoggerFactory.getLogger(PipelineService.class);

    private final LlmService llmService;
    private final TestCodeExtractor extractor;
    private final TestFileWriterService fileWriter;
    private final JestExecutionService jestExecutionService;

    public PipelineService(LlmService llmService,
                            TestCodeExtractor extractor,
                            TestFileWriterService fileWriter,
                            JestExecutionService jestExecutionService) {
        this.llmService = llmService;
        this.extractor = extractor;
        this.fileWriter = fileWriter;
        this.jestExecutionService = jestExecutionService;
    }

    public PipelineResult genererEcrireEtExecuter(String nomFonction, String codeSource) {
        return genererEcrireEtExecuter(nomFonction, codeSource, "projet-reference");
    }

    public PipelineResult genererEcrireEtExecuter(String nomFonction, String codeSource, String idProjet) {
        String projetIdFinal = (idProjet != null && !idProjet.isBlank()) ? idProjet : "projet-reference";

        String reponseLlm;
        try {
            reponseLlm = llmService.genererTest(nomFonction, codeSource);
        } catch (LlmCallException e) {
            log.warn("Echec de l'appel LLM pour {} : {}", nomFonction, e.getMessage());
            return PipelineResult.echecGeneration("Erreur LLM : " + e.getMessage());
        }

        String codeTest;
        try {
            codeTest = extractor.extraireCode(reponseLlm);
        } catch (TestGenerationException e) {
            log.warn("Reponse LLM invalide pour {} : {}", nomFonction, e.getMessage());
            return PipelineResult.echecGeneration("Reponse LLM invalide : " + e.getMessage());
        }

        Path fichier;
        try {
            fichier = "projet-reference".equals(projetIdFinal)
                    ? fileWriter.ecrireFichierTest(nomFonction, codeTest)
                    : fileWriter.ecrireFichierTest(nomFonction, codeTest, projetIdFinal);
        } catch (IOException e) {
            log.error("Erreur d'ecriture du fichier de test pour {}", nomFonction, e);
            return PipelineResult.echecGeneration("Erreur ecriture fichier : " + e.getMessage());
        }

        String cheminRelatif = "projet-reference".equals(projetIdFinal)
                ? fileWriter.cheminRelatifPourJest(nomFonction)
                : fileWriter.cheminRelatifPourJest(nomFonction, projetIdFinal);

        ExecutionTestResult resultatExecution = jestExecutionService.executerTest(cheminRelatif);

        if (resultatExecution.isErreurTechnique()) {
            log.error("Erreur technique lors de l'execution du test {} : {}",
                    fichier, resultatExecution.getMessageErreurTechnique());
            return PipelineResult.echecExecution(fichier.toString(), codeTest, resultatExecution.getMessageErreurTechnique());
        }

        JestRunResult resultatJest = resultatExecution.getResultatJest();
        log.info("Pipeline complet pour {} sur projet {} : {}/{} tests reussis",
                nomFonction, projetIdFinal, resultatJest.getTestsReussis(), resultatJest.getTotalTests());

        return PipelineResult.succes(
                fichier.toString(),
                codeTest,
                resultatJest.getTotalTests(),
                resultatJest.getTestsReussis(),
                resultatJest.getTestsEchoues()
        );
    }
}
