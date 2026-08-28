package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.service.ProjectService;
import com.smartautomation.testautomation.service.ProjectService.ProjectMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
public class ProjectUploadController {

    private static final Logger log = LoggerFactory.getLogger(ProjectUploadController.class);

    private final ProjectService projectService;

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir;

    public ProjectUploadController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/api/projets")
    public ResponseEntity<List<ProjectMetadata>> listerProjets() {
        return ResponseEntity.ok(projectService.listerProjets());
    }

    @PostMapping("/upload-projet")
    public ResponseEntity<?> uploadProjet(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pointEntree") String pointEntree,
            @RequestParam(value = "repositoryFile", required = false) String repositoryFile,
            @RequestParam(value = "serviceFile", required = false) String serviceFile,
            @RequestParam(value = "nomProjet", required = false) String nomProjet
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Fichier ZIP obligatoire"));
        }
        if (pointEntree == null || pointEntree.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Point d'entrée obligatoire (ex: app.js)"));
        }

        String originalFilename = file.getOriginalFilename();
        String slug = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase() : "projet";
        if (slug.endsWith("_zip")) {
            slug = slug.substring(0, slug.length() - 4);
        }
        String idProjet = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        String nomFinal = (nomProjet != null && !nomProjet.isBlank()) ? nomProjet : (originalFilename != null ? originalFilename : idProjet);

        Path workspacePath = Paths.get(scriptsDir, "workspaces", idProjet);

        try {
            // 1. Extraire le ZIP
            Files.createDirectories(workspacePath);
            extraireZip(file.getInputStream(), workspacePath);

            // 2. Vérifier que le point d'entrée existe dans le dossier extrait
            Path pointEntreePath = workspacePath.resolve(pointEntree);
            if (!Files.exists(pointEntreePath)) {
                log.warn("Point d'entrée {} non trouvé dans {}", pointEntree, workspacePath.toAbsolutePath());
            }

            // 3. Générer les configurations dynamiques Jest & Stryker
            genererJestConfig(idProjet, workspacePath);
            genererStrykerConfig(idProjet, workspacePath);

            // 4. Scanner les fichiers .js
            List<String> fichiers = projectService.listerFichiersJs(workspacePath);

            // 5. Enregistrer le projet dans ProjectService
            ProjectMetadata metadata = new ProjectMetadata(
                    idProjet,
                    nomFinal,
                    pointEntree,
                    (repositoryFile != null && !repositoryFile.isBlank()) ? repositoryFile : null,
                    (serviceFile != null && !serviceFile.isBlank()) ? serviceFile : null,
                    fichiers,
                    workspacePath
            );
            projectService.enregistrerProjet(metadata);

            return ResponseEntity.ok(metadata);

        } catch (IOException e) {
            log.error("Erreur lors de l'extraction ou de la sauvegarde du projet {}", idProjet, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erreur", "Erreur serveur lors de l'upload du projet : " + e.getMessage()));
        }
    }

    private void extraireZip(InputStream is, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = targetDir.resolve(entry.getName()).normalize();
                if (!filePath.startsWith(targetDir)) {
                    // Prévention Zip Slip
                    continue;
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private void genererJestConfig(String idProjet, Path workspacePath) throws IOException {
        String json = """
                {
                  "testEnvironment": "node",
                  "testMatch": [
                    "<rootDir>/generated-tests/%s/**/*.test.js"
                  ],
                  "modulePathIgnorePatterns": [
                    "<rootDir>/reports/",
                    "<rootDir>/coverage/"
                  ],
                  "collectCoverageFrom": [
                    "workspaces/%s/**/*.js"
                  ],
                  "coverageDirectory": "workspaces/%s/coverage/",
                  "coverageReporters": [
                    "text",
                    "text-summary",
                    "json-summary",
                    "lcov",
                    "html"
                  ],
                  "testTimeout": 30000
                }
                """.formatted(idProjet, idProjet, idProjet);

        Files.writeString(workspacePath.resolve("jest.config.json"), json, StandardCharsets.UTF_8);
    }

    private void genererStrykerConfig(String idProjet, Path workspacePath) throws IOException {
        String json = """
                {
                  "$schema": "../../node_modules/@stryker-mutator/core/schema/stryker-schema.json",
                  "mutate": [
                    "workspaces/%s/**/*.js"
                  ],
                  "testRunner": "jest",
                  "jest": {
                    "projectType": "custom",
                    "configFile": "workspaces/%s/jest.config.json",
                    "enableFindRelatedTests": true
                  },
                  "reporters": [
                    "clear-text",
                    "progress",
                    "json",
                    "html"
                  ],
                  "jsonReporter": {
                    "fileName": "workspaces/%s/reports/mutation/mutation.json"
                  },
                  "htmlReporter": {
                    "fileName": "workspaces/%s/reports/mutation/html/index.html"
                  },
                  "concurrency": 2,
                  "timeoutMS": 30000,
                  "tempDirName": "workspaces/%s/.stryker-tmp"
                }
                """.formatted(idProjet, idProjet, idProjet, idProjet, idProjet);

        Files.writeString(workspacePath.resolve("stryker.config.json"), json, StandardCharsets.UTF_8);
    }
}
