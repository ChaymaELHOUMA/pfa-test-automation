package com.smartautomation.testautomation.service;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.smartautomation.testautomation.exception.LlmCallException;
import com.smartautomation.testautomation.exception.TestGenerationException;
import com.smartautomation.testautomation.model.ResultatGeneration;

@Service
public class GenerationTestService {

    private static final Logger log = LoggerFactory.getLogger(GenerationTestService.class);

    private final LlmService llmService;
    private final TestCodeExtractor extractor;
    private final TestFileWriterService fileWriter;

    public GenerationTestService(LlmService llmService,
                                  TestCodeExtractor extractor,
                                  TestFileWriterService fileWriter) {
        this.llmService = llmService;
        this.extractor = extractor;
        this.fileWriter = fileWriter;
    }

    public ResultatGeneration genererEtSauvegarder(String nomFonction, String codeSource) {
        return genererEtSauvegarder(nomFonction, codeSource, "projet-reference");
    }

    public ResultatGeneration genererEtSauvegarder(String nomFonction, String codeSource, String idProjet) {
        String finalProjectId = (idProjet != null && !idProjet.isBlank()) ? idProjet : "projet-reference";
        String llmResponse;
        try {
            llmResponse = llmService.genererTest(nomFonction, codeSource);
        } catch (LlmCallException e) {
            log.warn("LLM call failed for function {}: {}", nomFonction, e.getMessage());
            return ResultatGeneration.echec("LLM error: " + e.getMessage());
        }

        String testCode;
        try {
            testCode = extractor.extraireCode(llmResponse);
        } catch (TestGenerationException e) {
            log.warn("Invalid LLM response for function {}: {}", nomFonction, e.getMessage());
            return ResultatGeneration.echec("Invalid LLM response: " + e.getMessage());
        }

        try {
            Path file = "projet-reference".equals(finalProjectId)
                    ? fileWriter.ecrireFichierTest(nomFonction, testCode)
                    : fileWriter.ecrireFichierTest(nomFonction, testCode, finalProjectId);
            log.info("Test generated successfully for {} on project {} -> {}", nomFonction, finalProjectId, file);
            return ResultatGeneration.succes(file.toString());
        } catch (IOException e) {
            log.error("Error writing test file for {}", nomFonction, e);
            return ResultatGeneration.echec("File write error: " + e.getMessage());
        }
    }
}
