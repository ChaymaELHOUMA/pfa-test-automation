package com.smartautomation.testautomation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("null")
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
                null,
                List.of("app.js"),
                workspaceDir
        );
        projectService.enregistrerProjet(metadata);

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
        String importPath = projectService.calculerEtValiderImportRelatif("projet-reference", "generated-tests", "fichierInexistant.js");
        assertNull(importPath, "Doit retourner null si le fichier n'existe pas sur le disque");
    }

    @Test
    void testConstruireContexteCouches_ServiceEtRepositoryExistent(@TempDir Path tempDir) throws IOException {
        Path workspaceDir = tempDir.resolve("workspaces/demo-proj-full");
        Files.createDirectories(workspaceDir);
        
        Path repoFile = workspaceDir.resolve("myRepo.js");
        Files.writeString(repoFile, "repo content");
        
        Path serviceFile = workspaceDir.resolve("myService.js");
        Files.writeString(serviceFile, "service content");
        
        ProjectService.ProjectMetadata metadata = new ProjectService.ProjectMetadata(
                "demo-proj-full", "Demo Project", "app.js", "myRepo.js", "myService.js", List.of(), workspaceDir
        );
        projectService.enregistrerProjet(metadata);
        
        org.springframework.test.util.ReflectionTestUtils.setField(projectService, "scriptsDir", tempDir.resolve("workspaces").toString());
        
        String resultat = projectService.construireContexteCouches("demo-proj-full", "demo-proj-full/routes.js");
        
        assertTrue(resultat.contains("--- Service (myService.js) ---"));
        assertTrue(resultat.contains("service content"));
        assertTrue(resultat.contains("--- Repository (myRepo.js) ---"));
        assertTrue(resultat.contains("repo content"));
        // Vérifie l'ordre : Service AVANT Repository
        assertTrue(resultat.indexOf("--- Service") < resultat.indexOf("--- Repository"));
    }

    @Test
    void testConstruireContexteCouches_RepositoryExiste(@TempDir Path tempDir) throws IOException {
        Path workspaceDir = tempDir.resolve("workspaces/demo-proj");
        Files.createDirectories(workspaceDir);
        Path repoFile = workspaceDir.resolve("myRepo.js");
        Files.writeString(repoFile, "repo content");
        
        ProjectService.ProjectMetadata metadata = new ProjectService.ProjectMetadata(
                "demo-proj", "Demo Project", "app.js", "myRepo.js", null, List.of(), workspaceDir
        );
        projectService.enregistrerProjet(metadata);
        
        org.springframework.test.util.ReflectionTestUtils.setField(projectService, "scriptsDir", tempDir.resolve("workspaces").toString());
        
        String resultat = projectService.construireContexteCouches("demo-proj", "demo-proj/routes.js");
        
        assertTrue(resultat.contains("--- Repository (myRepo.js) ---"));
        assertTrue(resultat.contains("repo content"));
        assertFalse(resultat.contains("--- Service"));
    }

    @Test
    void testConstruireContexteCouches_RepositoryNull() throws IOException {
        ProjectService.ProjectMetadata metadata = new ProjectService.ProjectMetadata(
                "demo-proj-2", "Demo 2", "app.js", null, null, List.of(), Path.of("fake")
        );
        projectService.enregistrerProjet(metadata);
        
        String resultat = projectService.construireContexteCouches("demo-proj-2", "demo-proj-2/routes.js");
        assertEquals("", resultat);
    }

    @Test
    void testConstruireContexteCouches_RepositoryNExistePasSurDisque(@TempDir Path tempDir) throws IOException {
        Path workspaceDir = tempDir.resolve("workspaces/demo-proj-3");
        Files.createDirectories(workspaceDir);
        
        ProjectService.ProjectMetadata metadata = new ProjectService.ProjectMetadata(
                "demo-proj-3", "Demo 3", "app.js", "fakeRepo.js", null, List.of(), workspaceDir
        );
        projectService.enregistrerProjet(metadata);
        
        org.springframework.test.util.ReflectionTestUtils.setField(projectService, "scriptsDir", tempDir.resolve("workspaces").toString());
        
        String resultat = projectService.construireContexteCouches("demo-proj-3", "demo-proj-3/routes.js");
        assertEquals("", resultat);
    }
}
