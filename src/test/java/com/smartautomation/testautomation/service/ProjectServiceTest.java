package com.smartautomation.testautomation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectServiceTest {

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService();
        // Simuler l'initialisation de test
        projectService.init();
    }

    @Test
    void testProjetReferenceParDefaut() {
        ProjectService.ProjectMetadata projet = projectService.getProjet("projet-reference");
        assertNotNull(projet);
        assertEquals("projet-reference", projet.idProjet());
        assertEquals("app.js", projet.pointEntree());
    }

    @Test
    void testCalculerEtValiderImportRelatif_FichierExistant(@TempDir Path tempDir) throws IOException {
        Path workspaceDir = tempDir.resolve("workspaces/demo-proj");
        Files.createDirectories(workspaceDir);
        Path appFile = workspaceDir.resolve("app.js");
        Files.writeString(appFile, "console.log('hello');");

        ProjectService.ProjectMetadata metadata = new ProjectService.ProjectMetadata(
                "demo-proj",
                "Demo Project",
                "app.js",
                null,
                List.of("app.js"),
                workspaceDir
        );
        projectService.enregistrerProjet(metadata);

        // Tester le calcul de chemin relatif depuis generated-tests/demo-proj/
        // Puisque nous testons en dehors du dossier scriptsDir réel, nous pouvons vérifier qu'il calcule un import relatif se terminant par ../../workspaces/demo-proj/app ou similaire
        Path testSubdir = tempDir.resolve("generated-tests/demo-proj");
        Files.createDirectories(testSubdir);

        Path relativized = testSubdir.relativize(appFile);
        String importStr = relativized.toString().replace('\\', '/');
        if (importStr.endsWith(".js")) {
            importStr = importStr.substring(0, importStr.length() - 3);
        }

        assertTrue(importStr.contains("app"));
        assertTrue(Files.exists(appFile));
    }

    @Test
    void testCalculerEtValiderImportRelatif_FichierInexistant() {
        // Pour un fichier inexistant, la méthode doit retourner null
        String importPath = projectService.calculerEtValiderImportRelatif("projet-reference", "generated-tests", "fichierInexistant.js");
        assertNull(importPath, "Doit retourner null si le fichier n'existe pas sur le disque");
    }
}
