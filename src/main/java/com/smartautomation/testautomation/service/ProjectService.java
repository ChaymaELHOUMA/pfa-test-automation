package com.smartautomation.testautomation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    public record ProjectMetadata(
            String idProjet,
            String nomProjet,
            String pointEntree,
            String repositoryFile,
            String serviceFile,
            List<String> fichiers,
            Path workspacePath
    ) {}

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir = "node-runner";

    private final Map<String, ProjectMetadata> projectsMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Register default reference project
        Path refWorkspace = Paths.get(scriptsDir, "projet-reference");
        List<String> refFiles = listerFichiersJs(refWorkspace);

        ProjectMetadata refMetadata = new ProjectMetadata(
                "projet-reference",
                "Reference Project",
                "app.js",
                "utilisateurRepository.js",
                "utilisateurService.js",
                refFiles,
                refWorkspace
        );
        projectsMap.put("projet-reference", refMetadata);
        log.info("Reference project initialized: {} files found", refFiles.size());
    }

    public void enregistrerProjet(ProjectMetadata metadata) {
        projectsMap.put(metadata.idProjet(), metadata);
        log.info("New project registered: id={}, name={}, entryPoint={}, repository={}, service={}",
                metadata.idProjet(), metadata.nomProjet(), metadata.pointEntree(), metadata.repositoryFile(), metadata.serviceFile());
    }

    public ProjectMetadata getProjet(String idProjet) {
        if (idProjet == null || idProjet.isBlank() || !projectsMap.containsKey(idProjet)) {
            return projectsMap.get("projet-reference");
        }
        return projectsMap.get(idProjet);
    }

    public List<ProjectMetadata> listerProjets() {
        return new ArrayList<>(projectsMap.values());
    }

    /**
     * Calculates the exact relative import path between the generated test folder
     * and the target file in the workspace, and VERIFIES its existence on disk.
     *
     * @param idProjet The active project ID
     * @param dossierTestSubpath The test destination subfolder (ex: "generated-tests/my-proj")
     * @param nomFichierCibleLeFichierDansWorkspace The target file name or path (ex: "app.js")
     * @return The import path as string (ex: "../../workspaces/my-proj/app") or null if file doesn't exist.
     */
    public String calculerEtValiderImportRelatif(String idProjet, String dossierTestSubpath, String nomFichierCibleLeFichierDansWorkspace) {
        ProjectMetadata metadata = getProjet(idProjet);
        if (nomFichierCibleLeFichierDansWorkspace == null || nomFichierCibleLeFichierDansWorkspace.isBlank()) {
            return null;
        }

        Path baseScripts = Paths.get(scriptsDir).toAbsolutePath().normalize();
        Path dossierTestAbs = baseScripts.resolve(dossierTestSubpath).normalize();
        Path fichierCibleAbs = metadata.workspacePath().toAbsolutePath().resolve(nomFichierCibleLeFichierDansWorkspace).normalize();

        // Pre-check file existence on filesystem
        if (!Files.exists(fichierCibleAbs)) {
            log.warn("VERIFICATION FAILED: Target file {} does not exist in workspace {}",
                    fichierCibleAbs.toAbsolutePath(), metadata.workspacePath().toAbsolutePath());
            return null;
        }

        Path cheminRelatif = dossierTestAbs.relativize(fichierCibleAbs);
        String importPathStr = cheminRelatif.toString().replace('\\', '/');

        // Remove .js extension if present for Node.js require
        if (importPathStr.endsWith(".js")) {
            importPathStr = importPathStr.substring(0, importPathStr.length() - 3);
        }

        if (!importPathStr.startsWith(".")) {
            importPathStr = "./" + importPathStr;
        }

        log.info("Import path calculated and validated successfully for {}: {}", nomFichierCibleLeFichierDansWorkspace, importPathStr);
        return importPathStr;
    }

    public List<String> listerFichiersJs(Path dir) {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".js"))
                    .map(p -> dir.relativize(p).toString().replace('\\', '/'))
                    .filter(path -> !path.startsWith("node_modules/") && !path.startsWith("generated-tests/"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.error("Error reading JS files in {}", dir, e);
            return List.of();
        }
    }

    /**
     * Resolves a file path (ex: "routes.js") to its relative location in scriptsDir
     * ex: "workspaces/idProjet/routes.js" or "projet-reference/routes.js"
     */
    public String resoudreCheminFichier(String idProjet, String cheminRelatif) {
        if (cheminRelatif == null || cheminRelatif.isBlank()) {
            return cheminRelatif;
        }
        if (cheminRelatif.startsWith("workspaces/") || cheminRelatif.startsWith("projet-reference/")) {
            return cheminRelatif;
        }
        ProjectMetadata metadata = getProjet(idProjet);
        Path rel = Paths.get(scriptsDir).toAbsolutePath().normalize().relativize(metadata.workspacePath().toAbsolutePath().resolve(cheminRelatif).normalize());
        return rel.toString().replace('\\', '/');
    }

    public String construireContexteCouches(String idProjet, String cheminFichierRoute) throws IOException {
        ProjectMetadata metadata = getProjet(idProjet);
        
        Path dossier = Paths.get(scriptsDir, cheminFichierRoute).getParent();
        if (dossier == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();

        // 1. Service
        if (metadata.serviceFile() != null && !metadata.serviceFile().isBlank()) {
            Path servicePath = dossier.resolve(metadata.serviceFile());
            if (Files.exists(servicePath)) {
                sb.append("--- Service (").append(servicePath.getFileName()).append(") ---")
                  .append(System.lineSeparator());
                sb.append(Files.readString(servicePath, java.nio.charset.StandardCharsets.UTF_8))
                  .append(System.lineSeparator()).append(System.lineSeparator());
            }
        }

        // 2. Repository
        if (metadata.repositoryFile() != null && !metadata.repositoryFile().isBlank()) {
            Path repoPath = dossier.resolve(metadata.repositoryFile());
            if (Files.exists(repoPath)) {
                sb.append("--- Repository (").append(repoPath.getFileName()).append(") ---")
                  .append(System.lineSeparator());
                sb.append(Files.readString(repoPath, java.nio.charset.StandardCharsets.UTF_8))
                  .append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
          
        return sb.toString();
    }
}
