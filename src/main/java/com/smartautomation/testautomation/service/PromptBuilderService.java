package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.BrancheInfo;
import com.smartautomation.testautomation.model.CasErreurInfo;
import com.smartautomation.testautomation.model.UniteTestable;
import com.smartautomation.testautomation.service.ProjectService.ProjectMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PromptBuilderService {

    private static final Logger log = LoggerFactory.getLogger(PromptBuilderService.class);

    private final ProjectService projectService;

    public PromptBuilderService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public String construirePromptCible(UniteTestable unite, String extraitCodeSource, List<String> testsExistants) {
        return construirePromptCible(unite, extraitCodeSource, testsExistants, "projet-reference");
    }

    public String construirePromptCible(UniteTestable unite, String extraitCodeSource, List<String> testsExistants, String idProjet) {
        StringBuilder sb = new StringBuilder();

        if (unite.isRoute()) {
            sb.append("Voici un handler de route Express : ")
              .append(unite.getMethode()).append(" ").append(unite.getChemin())
              .append(", avec les parametres (").append(String.join(", ", unite.getParametres())).append(").\n\n");
        } else {
            sb.append("Voici une fonction JavaScript nommee ").append(unite.getNom())
              .append(", avec les parametres (").append(String.join(", ", unite.getParametres())).append(").\n\n");
        }

        sb.append("Code source :\n").append(extraitCodeSource).append("\n\n");

        List<String> chemins = decrireCheminsACouvrir(unite);
        sb.append("Cette unite contient les chemins d'execution suivants, a couvrir chacun par un test dedie :\n");
        for (String c : chemins) {
            sb.append("- ").append(c).append("\n");
        }
        sb.append("\n");

        ajouterSectionTestsExistants(sb, testsExistants);

        sb.append("Genere PLUSIEURS tests unitaires Jest distincts, un test par categorie applicable :\n");
        sb.append("- cas nominal : entree valide standard\n");
        sb.append("- cas limite : 0, chaine vide \"\", tableau vide [], objet vide {}, valeur maximale\n");
        sb.append("- cas invalide : type incorrect, null, undefined, structure invalide\n");
        sb.append("- cas branche : chaque branche if/else ou chaque case d'un switch (y compris default)\n");
        sb.append("- cas erreur : chaque retour HTTP 4xx/5xx ou throw d'exception explicite\n\n");

        sb.append("Tu dois generer un test dedie pour CHAQUE chemin liste ci-dessus.\n");
        sb.append("Pour un switch, cree un test separe par case (default inclus).\n");
        sb.append("Pour une erreur HTTP, verifie explicitement le code de statut et le message d'erreur.\n\n");

        sb.append("FORMAT DE NOMMAGE STRICTEMENT OBLIGATOIRE (le nom du test doit COMMENCER EXACTEMENT par un de ces prefixe) :\n");
        sb.append("test('cas nominal - <description>', () => { ... })\n");
        sb.append("test('cas limite - <description>', () => { ... })\n");
        sb.append("test('cas invalide - <description>', () => { ... })\n");
        sb.append("test('cas branche - <description>', () => { ... })\n");
        sb.append("test('cas erreur - <description>', () => { ... })\n\n");

        sb.append("CONSIGNES DE SYNTAXE ET COMPILATION JEST (TRES IMPORTANT) :\n");
        sb.append("1. Pour tester qu'une fonction synchrone leve une erreur (throw), enveloppe TOUJOURS l'appel dans une fonction flechee : expect(() => maFonction(...)).toThrow();\n");
        sb.append("2. Pour les fonctions asynchrones, utilise await expect(maFonctionAsync(...)).rejects.toThrow();\n");
        sb.append("3. Assure-toi que toutes les variables et fonctions utilisees sont definies et accessibles.\n\n");

        if (unite.isRoute()) {
            ajouterInstructionsSupertest(sb, unite, idProjet);
        } else {
            sb.append("IMPORTANT POUR FONCTION STANDALONE :\n");
            sb.append("Le fichier de test doit etre 100% autonome et directement executable.\n");
            sb.append("Inclus la definition COMPLETE ET EXACTE de la fonction (et de toutes ses fonctions utilitaires) directement en haut du fichier de test.\n");
            sb.append("Ne fais AUCUN require() ni import vers un fichier externe (ex: ne fais PAS require('./...')).\n\n");
        }
        sb.append("Reponds uniquement avec du code Jest valide entoure de ```javascript et ```, sans aucun texte explicatif avant ou apres.");

        return sb.toString();
    }

    public String construirePromptBrut(UniteTestable unite, String extraitCodeSource) {
        return construirePromptBrut(unite, extraitCodeSource, List.of(), "projet-reference");
    }

    public String construirePromptBrut(UniteTestable unite, String extraitCodeSource, List<String> testsExistants) {
        return construirePromptBrut(unite, extraitCodeSource, testsExistants, "projet-reference");
    }

    public String construirePromptBrut(UniteTestable unite, String extraitCodeSource, List<String> testsExistants, String idProjet) {
        StringBuilder sb = new StringBuilder();

        if (unite.isRoute()) {
            sb.append("Voici un handler de route Express : ")
              .append(unite.getMethode()).append(" ").append(unite.getChemin()).append(".\n\n");
        } else {
            sb.append("Voici une fonction JavaScript nommee ").append(unite.getNom()).append(".\n\n");
        }

        sb.append("Code source :\n").append(extraitCodeSource).append("\n\n");

        ajouterSectionTestsExistants(sb, testsExistants);

        sb.append("Genere PLUSIEURS tests unitaires Jest distincts pour ce code, un test par categorie applicable :\n");
        sb.append("- cas nominal : entree valide standard\n");
        sb.append("- cas limite : 0, chaine vide \"\", tableau vide [], objet vide {}\n");
        sb.append("- cas invalide : type incorrect, null, undefined\n");
        sb.append("- cas branche : chaque branche if/else ou chaque case d'un switch (y compris default)\n");
        sb.append("- cas erreur : chaque retour HTTP 4xx/5xx ou throw explicite\n\n");

        sb.append("FORMAT DE NOMMAGE STRICTEMENT OBLIGATOIRE (le nom du test doit COMMENCER EXACTEMENT par un de ces prefixe) :\n");
        sb.append("test('cas nominal - <description>', () => { ... })\n");
        sb.append("test('cas limite - <description>', () => { ... })\n");
        sb.append("test('cas invalide - <description>', () => { ... })\n");
        sb.append("test('cas branche - <description>', () => { ... })\n");
        sb.append("test('cas erreur - <description>', () => { ... })\n\n");

        sb.append("CONSIGNES DE SYNTAXE ET COMPILATION JEST (TRES IMPORTANT) :\n");
        sb.append("1. Pour tester qu'une fonction synchrone leve une erreur (throw), enveloppe TOUJOURS l'appel dans une fonction flechee : expect(() => maFonction(...)).toThrow();\n");
        sb.append("2. Pour les fonctions asynchrones, utilise await expect(maFonctionAsync(...)).rejects.toThrow();\n\n");

        if (unite.isRoute()) {
            ajouterInstructionsSupertest(sb, unite, idProjet);
        } else {
            sb.append("IMPORTANT POUR FONCTION STANDALONE :\n");
            sb.append("Le fichier de test doit etre 100% autonome et directement executable.\n");
            sb.append("Inclus la definition COMPLETE ET EXACTE de la fonction (et de toutes ses fonctions utilitaires) directement en haut du fichier de test.\n");
            sb.append("Ne fais AUCUN require() ni import vers un fichier externe.\n\n");
        }
        sb.append("Reponds uniquement avec du code Jest valide entoure de ```javascript et ```, sans aucun texte explicatif avant ou apres.");

        return sb.toString();
    }

    private void ajouterSectionTestsExistants(StringBuilder sb, List<String> testsExistants) {
        if (testsExistants != null && !testsExistants.isEmpty()) {
            sb.append("Ne genere pas de test deja present dans cette liste :\n");
            for (String t : testsExistants) {
                sb.append("- ").append(t).append("\n");
            }
            sb.append("\n");
        }
    }

    private void ajouterInstructionsSupertest(StringBuilder sb, UniteTestable unite, String idProjet) {
        ProjectMetadata metadata = projectService.getProjet(idProjet);
        String subfolder = (idProjet == null || idProjet.equals("projet-reference")) ? "generated-tests" : "generated-tests/" + idProjet;

        String appImport = projectService.calculerEtValiderImportRelatif(idProjet, subfolder, metadata.pointEntree());
        String repoImport = metadata.repositoryFile() != null ? projectService.calculerEtValiderImportRelatif(idProjet, subfolder, metadata.repositoryFile()) : null;

        sb.append("IMPORTANT POUR ROUTE EXPRESS (SUPERTEST) :\n");
        sb.append("Ce handler est une route Express (req/res).\n");
        sb.append("Utilise Supertest sur l'application Express COMPLETE — ne l'appelle PAS directement avec des objets req/res mockes.\n");
        sb.append("NE recree PAS une mini-app Express avec le handler copie/colle.\n");
        sb.append("NE utilise PAS jest.mock(), jest.fn() ni aucun mock de service/repository/module interne.\n");
        sb.append("CHEMINS DE REQUIRE STRICTEMENT OBLIGATOIRES DEPUIS ").append(subfolder).append("/ (copier exactement ces lignes) :\n");
        sb.append("  const request = require('supertest');\n");
        if (appImport != null) {
            sb.append("  const app = require('").append(appImport).append("');\n");
        } else {
            log.warn("Impossible de calculer le chemin d'import pour {} avec idProjet={}, subfolder={}, pointEntree={}",
                    metadata.pointEntree(), idProjet, subfolder, metadata.pointEntree());
            sb.append("  // ERREUR: chemin d'import impossible à calculer - le fichier cible n'existe pas\n");
            sb.append("  // const app = require('CHEMIN_A_CORRIGER');\n");
        }

        if (repoImport != null) {
            sb.append("  const { reinitialiserEtat } = require('").append(repoImport).append("');\n");
            sb.append("  beforeEach(() => { reinitialiserEtat(); });\n\n");
        } else {
            sb.append("\n");
        }

        sb.append("Exemple de syntaxe attendue :\n");
        sb.append("const request = require('supertest');\n");
        if (appImport != null) {
            sb.append("const app = require('").append(appImport).append("');\n");
        } else {
            sb.append("// const app = require('CHEMIN_A_CORRIGER');\n");
        }
        if (repoImport != null) {
            sb.append("const { reinitialiserEtat } = require('").append(repoImport).append("');\n");
            sb.append("beforeEach(() => { reinitialiserEtat(); });\n");
        }
        sb.append("test('cas nominal - <description>', async () => {\n");
        sb.append("  const reponse = await request(app).")
          .append(unite.getMethode().toLowerCase()).append("('")
          .append(exempleCheminSupertest(unite.getChemin())).append("').send({ ... });\n");
        sb.append("  expect(reponse.statusCode).toBe(200);\n");
        sb.append("  expect(reponse.body).toEqual({ ... });\n");
        sb.append("});\n");
        sb.append("test('cas erreur - <description>', async () => {\n");
        sb.append("  const reponse = await request(app).")
          .append(unite.getMethode().toLowerCase()).append("('")
          .append(exempleCheminSupertest(unite.getChemin())).append("').send({ ... });\n");
        sb.append("  expect(reponse.statusCode).toBe(400);\n");
        sb.append("});\n");
        sb.append("Utilise async/await pour chaque test HTTP Supertest. Verifie le statusCode (ou status) et response.body.\n\n");
    }

    private String exempleCheminSupertest(String chemin) {
        return chemin.replace(":id", "1").replace(":userId", "1");
    }

    private List<String> decrireCheminsACouvrir(UniteTestable unite) {
        List<String> chemins = new ArrayList<>();
        chemins.add("le cas nominal (chemin normal, sans erreur)");

        if (unite.getBranches() != null) {
            for (BrancheInfo b : unite.getBranches()) {
                if ("if".equals(b.getType())) {
                    chemins.add("le cas ou la condition if (ligne " + b.getLigne() + ") est vraie");
                    if (Boolean.TRUE.equals(b.getAUnElse())) {
                        chemins.add("le cas ou la condition if (ligne " + b.getLigne() + ") est fausse (branche else)");
                    }
                } else if ("switch".equals(b.getType())) {
                    chemins.add("chacun des " + b.getNombreCas() + " cas du switch (ligne " + b.getLigne() + ")");
                }
            }
        }

        if (unite.getCasErreur() != null) {
            for (CasErreurInfo e : unite.getCasErreur()) {
                if ("reponse-erreur-http".equals(e.getType())) {
                    chemins.add("le cas d'erreur renvoyant le code HTTP " + e.getCodeHttp() + " (ligne " + e.getLigne() + ")");
                } else if ("try-catch".equals(e.getType())) {
                    chemins.add("le cas ou une exception est levee dans le bloc try (ligne " + e.getLigne() + ")");
                } else if ("throw".equals(e.getType())) {
                    chemins.add("le cas ou une erreur est explicitement levee (throw, ligne " + e.getLigne() + ")");
                }
            }
        }

        return chemins;
    }

    public List<String> decrireCheminsACouvrirPublic(UniteTestable unite) {
        return decrireCheminsACouvrir(unite);
    }

    public String construirePromptIntegration(UniteTestable unite, String extraitRoute, String contexteCouches) {
        return construirePromptIntegration(unite, extraitRoute, contexteCouches, List.of(), "projet-reference");
    }

    public String construirePromptIntegration(UniteTestable unite, String extraitRoute, String contexteCouches, List<String> testsExistants) {
        return construirePromptIntegration(unite, extraitRoute, contexteCouches, testsExistants, "projet-reference");
    }

    public String construirePromptIntegration(UniteTestable unite, String extraitRoute, String contexteCouches, List<String> testsExistants, String idProjet) {
        ProjectMetadata metadata = projectService.getProjet(idProjet);
        String subfolder = (idProjet == null || idProjet.equals("projet-reference")) ? "generated-tests" : "generated-tests/" + idProjet;

        String appImport = projectService.calculerEtValiderImportRelatif(idProjet, subfolder, metadata.pointEntree());
        String repoImport = metadata.repositoryFile() != null ? projectService.calculerEtValiderImportRelatif(idProjet, subfolder, metadata.repositoryFile()) : null;

        StringBuilder sb = new StringBuilder();

        sb.append("Voici une route Express a tester en INTEGRATION (plusieurs couches) : ")
          .append(unite.getMethode()).append(" ").append(unite.getChemin()).append(".\n\n");

        sb.append("Code du controleur (handler de route) :\n").append(extraitRoute).append("\n\n");

        if (contexteCouches != null && !contexteCouches.isBlank()) {
            sb.append("Couches sous-jacentes reelles (service, repository) — NE PAS MOCKER :\n");
            sb.append(contexteCouches).append("\n\n");
        }

        ajouterSectionTestsExistants(sb, testsExistants);

        sb.append("Genere UNIQUEMENT des tests d'INTEGRATION Jest (pas de tests unitaires isoles).\n");
        sb.append("Regles obligatoires :\n");
        sb.append("- Ne mocke AUCUNE fonction/service/repository interne : exercice le vrai chemin route -> service -> donnees.\n");
        sb.append("- Utilise Supertest sur l'application Express complete.\n");
        sb.append("  CHEMIN EXACT OBLIGATOIRE depuis ").append(subfolder).append("/ (ne pas raccourcir) :\n");
        if (appImport != null) {
            sb.append("    const app = require('").append(appImport).append("');\n");
        } else {
            log.warn("Impossible de calculer le chemin d'import pour {} avec idProjet={}, subfolder={}, pointEntree={}",
                    metadata.pointEntree(), idProjet, subfolder, metadata.pointEntree());
            sb.append("    // ERREUR: chemin d'import impossible à calculer - le fichier cible n'existe pas\n");
            sb.append("    // const app = require('CHEMIN_A_CORRIGER');\n");
        }
        if (repoImport != null) {
            sb.append("    const { reinitialiserEtat } = require('").append(repoImport).append("');\n");
        }
        sb.append("- Verifie le code HTTP ET la structure du body de reponse.\n");
        if (repoImport != null) {
            sb.append("- VERIFICATION DE PERSISTANCE : Si la route modifie un etat (ex: PUT, POST, DELETE), refais un GET (ou un second appel) pour confirmer ");
            sb.append("que le changement a bien mepris effet et persiste dans la couche de donnees, pas seulement dans la reponse immediate.\n");
            sb.append("- En cas d'erreur de validation (ex: statut invalide -> 400), refais aussi un GET pour verifier que les donnees n'ont PAS ete modifiees.\n");
            sb.append("- Reinitialise l'etat partage entre les tests si necessaire (ex: beforeEach avec reinitialiserEtat ");
            sb.append("du repository) pour garantir l'isolation.\n\n");
        }

        sb.append("FORMAT DE NOMMAGE STRICTEMENT OBLIGATOIRE (prefixe d'integration OBLIGATOIRE) :\n");
        sb.append("test('integration - <description>', async () => { ... })\n\n");

        sb.append("Syntaxe attendue (copier ces lignes require EXACTEMENT, sans modification) :\n");
        sb.append("const request = require('supertest');\n");
        if (appImport != null) {
            sb.append("const app = require('").append(appImport).append("');\n");
        } else {
            sb.append("// const app = require('CHEMIN_A_CORRIGER');\n");
        }
        if (repoImport != null) {
            sb.append("const { reinitialiserEtat } = require('").append(repoImport).append("');\n");
            sb.append("beforeEach(() => { reinitialiserEtat(); });\n");
        }
        sb.append("test('integration - <description>', async () => {\n");
        sb.append("  const reponsePut = await request(app).")
          .append(unite.getMethode().toLowerCase()).append("('")
          .append(exempleCheminSupertest(unite.getChemin())).append("').send({ ... });\n");
        sb.append("  expect(reponsePut.statusCode).toBe(200);\n");
        sb.append("});\n\n");

        sb.append("Reponds uniquement avec du code Jest valide entoure de ```javascript et ```, sans aucun texte explicatif avant ou apres.");

        return sb.toString();
    }
}
