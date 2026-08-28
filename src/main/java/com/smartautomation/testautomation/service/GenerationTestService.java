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
        String projetIdFinal = (idProjet != null && !idProjet.isBlank()) ? idProjet : "projet-reference";
        String reponseLlm;
        try {
            reponseLlm = llmService.genererTest(nomFonction, codeSource);
        } catch (LlmCallException e) {
            log.warn("Echec de l'appel LLM pour la fonction {} : {}", nomFonction, e.getMessage());
            return ResultatGeneration.echec("Erreur LLM : " + e.getMessage());
        }

        String codeTest;
        try {
            codeTest = extractor.extraireCode(reponseLlm);
        } catch (TestGenerationException e) {
            log.warn("Reponse LLM invalide pour la fonction {} : {}", nomFonction, e.getMessage());
            return ResultatGeneration.echec("Reponse LLM invalide : " + e.getMessage());
        }

        try {
            Path fichier = "projet-reference".equals(projetIdFinal)
                    ? fileWriter.ecrireFichierTest(nomFonction, codeTest)
                    : fileWriter.ecrireFichierTest(nomFonction, codeTest, projetIdFinal);
            log.info("Test genere avec succes pour {} sur projet {} -> {}", nomFonction, projetIdFinal, fichier);
            return ResultatGeneration.succes(fichier.toString());
        } catch (IOException e) {
            log.error("Erreur d'ecriture du fichier de test pour {}", nomFonction, e);
            return ResultatGeneration.echec("Erreur ecriture fichier : " + e.getMessage());
        }
    }
}
