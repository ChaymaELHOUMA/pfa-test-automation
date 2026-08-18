package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.BrancheInfo;
import com.smartautomation.testautomation.model.CasErreurInfo;
import com.smartautomation.testautomation.model.UniteTestable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PromptBuilderService {

    public String construirePromptCible(UniteTestable unite, String extraitCodeSource, List<String> testsExistants) {
        StringBuilder sb = new StringBuilder();

        if (unite.isRoute()) {
            sb.append("Voici un handler de route Express : ")
              .append(unite.getMethode()).append(" ").append(unite.getChemin())
              .append(", avec les parametres (").append(String.join(", ", unite.getParametres())).append(").\n\n");
        } else {
            sb.append("Voici une fonction JavaScript nommee ").append(unite.getNom())
              .append(", avec les parametres (").append(String.join(", ", unite.getParametres())).append(").\n\n");
        }

        sb.append("Code source :\n").append(extraitCodeSource).append("\n");

        List<String> chemins = decrireCheminsACouvrir(unite);
        sb.append("Cette unite contient les chemins d'execution suivants, a couvrir chacun par un test :\n");
        for (String c : chemins) {
            sb.append("- ").append(c).append("\n");
        }
        sb.append("\n");

        ajouterSectionTestsExistants(sb, testsExistants);

        sb.append("Genere PLUSIEURS tests unitaires Jest distincts, un test par categorie applicable :\n");
        sb.append("- cas nominal : entree valide standard\n");
        sb.append("- cas limite : 0, chaine vide, tableau vide, valeur maximale\n");
        sb.append("- cas invalide : type incorrect, null/undefined\n");
        sb.append("- cas branche : chaque branche if/else ou chaque case d'un switch\n");
        sb.append("- cas erreur : chaque retour HTTP 4xx/5xx ou throw explicite\n\n");

        sb.append("Tu dois generer un test dedie pour CHAQUE chemin liste ci-dessus.\n");
        sb.append("Pour un switch, cree un test separe par case (default inclus).\n");
        sb.append("Pour une erreur HTTP, verifie explicitement le code de statut dans le test.\n\n");

        sb.append("Format de nommage OBLIGATOIRE (utilise exactement une de ces categories) :\n");
        sb.append("test('cas nominal - <description>', () => { ... })\n");
        sb.append("test('cas limite - <description>', () => { ... })\n");
        sb.append("test('cas invalide - <description>', () => { ... })\n");
        sb.append("test('cas branche - <description>', () => { ... })\n");
        sb.append("test('cas erreur - <description>', () => { ... })\n\n");

        if (unite.isRoute()) {
            ajouterInstructionsSupertest(sb, unite);
        } else {
            sb.append("IMPORTANT : le fichier de test doit etre autonome et directement executable. ");
            sb.append("Inclus la definition complete de la fonction directement dans le fichier de test ");
            sb.append("(ne fais AUCUN require ni import vers un fichier externe, puisque ce fichier n'existe pas).\n");
        }
        sb.append("Reponds uniquement avec du code Jest, sans texte explicatif ni balises markdown.");

        return sb.toString();
    }
    
    public String construirePromptBrut(UniteTestable unite, String extraitCodeSource) {
        return construirePromptBrut(unite, extraitCodeSource, List.of());
    }

    public String construirePromptBrut(UniteTestable unite, String extraitCodeSource, List<String> testsExistants) {
        StringBuilder sb = new StringBuilder();

        if (unite.isRoute()) {
            sb.append("Voici un handler de route Express : ")
              .append(unite.getMethode()).append(" ").append(unite.getChemin()).append(".\n\n");
        } else {
            sb.append("Voici une fonction JavaScript nommee ").append(unite.getNom()).append(".\n\n");
        }

        sb.append("Code source :\n").append(extraitCodeSource).append("\n");

        ajouterSectionTestsExistants(sb, testsExistants);
        
        sb.append("Genere PLUSIEURS tests unitaires Jest distincts pour ce code, un test par categorie applicable :\n");
        sb.append("- cas nominal : entree valide standard\n");
        sb.append("- cas limite : 0, chaine vide, tableau vide, valeur maximale\n");
        sb.append("- cas invalide : type incorrect, null/undefined\n");
        sb.append("- cas branche : chaque branche if/else ou chaque case d'un switch\n");
        sb.append("- cas erreur : chaque retour HTTP 4xx/5xx ou throw explicite\n\n");
        sb.append("Pour un switch, cree un test separe par case (default inclus).\n");
        sb.append("Pour une erreur HTTP, verifie explicitement le code de statut dans le test.\n\n");

        sb.append("Format de nommage OBLIGATOIRE (utilise exactement une de ces categories) :\n");
        sb.append("test('cas nominal - <description>', () => { ... })\n");
        sb.append("test('cas limite - <description>', () => { ... })\n");
        sb.append("test('cas invalide - <description>', () => { ... })\n");
        sb.append("test('cas branche - <description>', () => { ... })\n");
        sb.append("test('cas erreur - <description>', () => { ... })\n\n");

        if (unite.isRoute()) {
            ajouterInstructionsSupertest(sb, unite);
        } else {
            sb.append("IMPORTANT : le fichier de test doit etre autonome et directement executable. ");
            sb.append("Inclus la definition complete de la fonction directement dans le fichier de test ");
            sb.append("(ne fais AUCUN require ni import vers un fichier externe, puisque ce fichier n'existe pas).\n");
        }
        sb.append("Reponds uniquement avec du code Jest, sans texte explicatif ni balises markdown.");

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

    private void ajouterInstructionsSupertest(StringBuilder sb, UniteTestable unite) {
        sb.append("IMPORTANT : ce handler est une route Express (req/res). ");
        sb.append("Utilise Supertest sur l'application Express COMPLETE — ne l'appelle PAS directement avec des objets req/res mockes.\n");
        sb.append("NE recree PAS une mini-app Express avec le handler copie/colle.\n");
        sb.append("NE utilise PAS jest.mock(), jest.fn() ni aucun mock de service/repository/module interne ");
        sb.append("(ex: utilisateurService, utilisateurRepository) : le handler delegue a des couches reelles.\n");
        sb.append("Charge l'app montee depuis le projet de reference :\n");
        sb.append("  const app = require('../projet-reference/app');\n");
        sb.append("Reinitialise l'etat partage entre les tests si la route modifie des donnees :\n");
        sb.append("  const { reinitialiserEtat } = require('../projet-reference/utilisateurRepository');\n");
        sb.append("  beforeEach(() => { reinitialiserEtat(); });\n");
        sb.append("Chaque test unitaire couvre UN seul chemin/cas (nominal, branche, erreur...) via un appel HTTP Supertest.\n");
        sb.append("Syntaxe attendue :\n");
        sb.append("const request = require('supertest');\n");
        sb.append("const app = require('../projet-reference/app');\n");
        sb.append("const { reinitialiserEtat } = require('../projet-reference/utilisateurRepository');\n");
        sb.append("beforeEach(() => { reinitialiserEtat(); });\n");
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
        sb.append("Utilise async/await. Verifie statusCode (ou status) et response.body.\n\n");
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

    /**
     * Prompt dedie aux tests d'integration multi-couches (controleur + service + repository).
     * Distinct des prompts unitaires brut/structure : ne remplace pas leur generation.
     */
    public String construirePromptIntegration(UniteTestable unite, String extraitRoute, String contexteCouches) {
        return construirePromptIntegration(unite, extraitRoute, contexteCouches, List.of());
    }

    public String construirePromptIntegration(UniteTestable unite, String extraitRoute, String contexteCouches, List<String> testsExistants) {
        StringBuilder sb = new StringBuilder();

        sb.append("Voici une route Express a tester en INTEGRATION (plusieurs couches) : ")
          .append(unite.getMethode()).append(" ").append(unite.getChemin()).append(".\n\n");

        sb.append("Code du controleur (handler de route) :\n").append(extraitRoute).append("\n");

        if (contexteCouches != null && !contexteCouches.isBlank()) {
            sb.append("Couches sous-jacentes reelles (service, repository) — NE PAS MOCKER :\n");
            sb.append(contexteCouches).append("\n");
        }

        ajouterSectionTestsExistants(sb, testsExistants);

        sb.append("Genere UNIQUEMENT des tests d'INTEGRATION Jest (pas de tests unitaires isoles).\n");
        sb.append("Regles obligatoires :\n");
        sb.append("- Ne mocke AUCUNE fonction/service/repository interne : exercice le vrai chemin route -> service -> donnees.\n");
        sb.append("- Utilise Supertest sur l'application Express complete.\n");
        sb.append("  CHEMIN EXACT OBLIGATOIRE depuis generated-tests/ (ne pas raccourcir) :\n");
        sb.append("    const app = require('../projet-reference/app');\n");
        sb.append("    const { reinitialiserEtat } = require('../projet-reference/utilisateurRepository');\n");
        sb.append("  INTERDIT : require('../app'), require('./app'), require('../utilisateurRepository').\n");
        sb.append("- Verifie le code HTTP ET la structure du body de reponse.\n");
        sb.append("- Si la route modifie un etat (ex: PUT), refais un GET (ou un second appel) pour confirmer ");
        sb.append("que le changement a bien persiste dans la couche de donnees, pas seulement la reponse immediate.\n");
        sb.append("- Reinitialise l'etat partage entre les tests si necessaire (ex: beforeEach avec reinitialiserEtat ");
        sb.append("du repository) pour garantir l'isolation.\n\n");

        sb.append("Format de nommage OBLIGATOIRE (prefixe distinct des tests unitaires) :\n");
        sb.append("test('integration - <description>', async () => { ... })\n\n");

        sb.append("Syntaxe attendue (copier ces lignes require EXACTEMENT, sans modification) :\n");
        sb.append("const request = require('supertest');\n");
        sb.append("const app = require('../projet-reference/app');\n");
        sb.append("const { reinitialiserEtat } = require('../projet-reference/utilisateurRepository');\n");
        sb.append("beforeEach(() => { reinitialiserEtat(); });\n");
        sb.append("test('integration - <description>', async () => {\n");
        sb.append("  const reponsePut = await request(app).")
          .append(unite.getMethode().toLowerCase()).append("('")
          .append(exempleCheminSupertest(unite.getChemin())).append("').send({ ... });\n");
        sb.append("  expect(reponsePut.statusCode).toBe(200);\n");
        sb.append("  const reponseGet = await request(app).get('")
          .append(exempleCheminSupertest(unite.getChemin())).append("');\n");
        sb.append("  expect(reponseGet.body.statut).toBe(...);\n");
        sb.append("});\n\n");

        sb.append("Genere au moins un test nominal (succes complet multi-couches) et un test d'erreur ");
        sb.append("si applicable (ex: statut invalide -> 400 sans modification de l'etat).\n");
        sb.append("Reponds uniquement avec du code Jest, sans texte explicatif ni balises markdown.");

        return sb.toString();
    }

}
