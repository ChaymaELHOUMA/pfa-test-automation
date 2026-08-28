package com.smartautomation.testautomation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
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
            List<String> fichiers,
            Path workspacePath
    ) {}

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir = "node-runner";

    private final Map<String, ProjectMetadata> projectsMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Enregistrer le projet de référence par défaut
        Path refWorkspace = Paths.get(scriptsDir, "projet-reference");
        List<String> refFiles = listerFichiersJs(refWorkspace);

        ProjectMetadata refMetadata = new ProjectMetadata(
                "projet-reference",
                "Projet de Référence",
                "app.js",
                "utilisateurRepository.js",
                refFiles,
                refWorkspace
        );
        projectsMap.put("projet-reference", refMetadata);
        log.info("Projet de référence initialisé : {} fichiers trouvés", refFiles.size());
    }

    public void enregistrerProjet(ProjectMetadata metadata) {
        projectsMap.put(metadata.idProjet(), metadata);
        log.info("Nouveau projet enregistré : id={}, nom={}, pointEntree={}, repository={}",
                metadata.idProjet(), metadata.nomProjet(), metadata.pointEntree(), metadata.repositoryFile());
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
     * Calcule le chemin d'importation relatif exact entre le dossier du test généré
     * et le fichier cible dans le workspace, et VÉRIFIE son existence sur le disque.
     *
     * @param idProjet L'ID du projet actif
     * @param dossierTestSubpath Le sous-dossier de destination du test (ex: "generated-tests/my-proj")
     * @param nomFichierCibleLeFichierDansWorkspace Le nom ou chemin du fichier cible (ex: "app.js")
     * @return Le chemin d'importation sous forme de chaîne (ex: "../../workspaces/my-proj/app") ou null si le fichier n'existe pas.
     */
    public String calculerEtValiderImportRelatif(String idProjet, String dossierTestSubpath, String nomFichierCibleLeFichierDansWorkspace) {
        ProjectMetadata metadata = getProjet(idProjet);
        if (nomFichierCibleLeFichierDansWorkspace == null || nomFichierCibleLeFichierDansWorkspace.isBlank()) {
            return null;
        }

        Path baseScripts = Paths.get(scriptsDir).toAbsolutePath().normalize();
        Path dossierTestAbs = baseScripts.resolve(dossierTestSubpath).normalize();
        Path fichierCibleAbs = metadata.workspacePath().toAbsolutePath().resolve(nomFichierCibleLeFichierDansWorkspace).normalize();

        // Vérification préalable d'existence sur le système de fichiers
        if (!Files.exists(fichierCibleAbs)) {
            log.warn("VÉRIFICATION ÉCHOUÉE : Le fichier cible {} n'existe pas dans le workspace {}",
                    fichierCibleAbs.toAbsolutePath(), metadata.workspacePath().toAbsolutePath());
            return null;
        }

        Path cheminRelatif = dossierTestAbs.relativize(fichierCibleAbs);
        String importPathStr = cheminRelatif.toString().replace('\\', '/');

        // Supprimer l'extension .js si présente pour le require Node.js
        if (importPathStr.endsWith(".js")) {
            importPathStr = importPathStr.substring(0, importPathStr.length() - 3);
        }

        if (!importPathStr.startsWith(".")) {
            importPathStr = "./" + importPathStr;
        }

        log.info("Chemin d'importation calculé et validé avec succès pour {}: {}", nomFichierCibleLeFichierDansWorkspace, importPathStr);
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
            log.error("Erreur lors de la lecture des fichiers JS dans {}", dir, e);
            return List.of();
        }
    }

    /**
     * Résout un chemin de fichier (ex: "routes.js") vers son emplacement relatif dans scriptsDir
     * ex: "workspaces/idProjet/routes.js" ou "projet-reference/routes.js"
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
}
