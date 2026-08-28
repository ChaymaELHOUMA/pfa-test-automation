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
        return ecrireFichierTest(nomFonction, codeTest, "projet-reference");
    }

    public Path ecrireFichierTest(String nomFonction, String codeTest, String idProjet) throws IOException {
        Path dossier = getDossierOutput(idProjet);
        if (!Files.exists(dossier)) {
            Files.createDirectories(dossier);
        }

        String nomFichier = normaliserNomFichier(nomFonction);
        Path fichier = dossier.resolve(nomFichier);

        Files.writeString(fichier, codeTest, StandardCharsets.UTF_8);
        return fichier;
    }

    public String lireContenuFichierTest(String nomFichier, String idProjet) throws IOException {
        Path dossier = getDossierOutput(idProjet);
        Path fichier = dossier.resolve(nomFichier);
        if (!Files.exists(fichier) && !nomFichier.endsWith(".test.js")) {
            fichier = dossier.resolve(nomFichier + ".test.js");
        }
        if (!Files.exists(fichier)) {
            throw new IOException("Fichier de test " + nomFichier + " introuvable");
        }
        return Files.readString(fichier, StandardCharsets.UTF_8);
    }

    public String cheminRelatifPourJest(String nomFonction) {
        return cheminRelatifPourJest(nomFonction, "projet-reference");
    }

    public String cheminRelatifPourJest(String nomFonction, String idProjet) {
        String nomFichier = normaliserNomFichier(nomFonction);
        if (idProjet == null || idProjet.isBlank() || "projet-reference".equals(idProjet)) {
            return "generated-tests/" + nomFichier;
        }
        return "generated-tests/" + idProjet + "/" + nomFichier;
    }

    public Path getDossierOutput(String idProjet) {
        if (idProjet == null || idProjet.isBlank() || "projet-reference".equals(idProjet)) {
            return Path.of(outputDir);
        }
        return Path.of(outputDir, idProjet);
    }

    private String normaliserNomFichier(String nomFonction) {
        if (nomFonction.endsWith(".test.js")) {
            return nomFonction;
        }
        return nomFonction.replaceAll("[^a-zA-Z0-9_-]", "_") + ".test.js";
    }
}
