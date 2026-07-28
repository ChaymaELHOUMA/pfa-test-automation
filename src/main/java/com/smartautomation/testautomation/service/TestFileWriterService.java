package com.smartautomation.testautomation.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class TestFileWriterService {

    @Value("${test.output-dir:generated-tests}")
    private String outputDir;

    
    public Path ecrireFichierTest(String nomFonction, String codeTest) throws IOException {
        Path dossier = Path.of(outputDir);
        if (!Files.exists(dossier)) {
            Files.createDirectories(dossier);
        }

        String nomFichier = nomFonction.replaceAll("[^a-zA-Z0-9_-]", "_") + ".test.js";
        Path fichier = dossier.resolve(nomFichier);

        Files.writeString(fichier, codeTest, StandardCharsets.UTF_8);
        return fichier;
    }

    public String cheminRelatifPourJest(String nomFonction) {
        String nomFichier = nomFonction.replaceAll("[^a-zA-Z0-9_-]", "_") + ".test.js";
        
        return "generated-tests/" + nomFichier;
    }
}

