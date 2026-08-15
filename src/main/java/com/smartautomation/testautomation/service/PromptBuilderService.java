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

        if (testsExistants != null && !testsExistants.isEmpty()) {
            sb.append("Ne genere pas de test deja present dans cette liste :\n");
            for (String t : testsExistants) {
                sb.append("- ").append(t).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Genere PLUSIEURS tests unitaires Jest distincts");
        if (unite.isRoute()) {
            sb.append(" (avec Supertest si pertinent)");
        }
        sb.append(", un test par categorie applicable :\n");
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
            sb.append("IMPORTANT : Pour les routes Express, utilise OBLIGATOIREMENT Supertest avec l'application reelle. ");
            sb.append("Importe l'application depuis projet-reference/app.js qui contient le router monté.\n");
            sb.append("Exemple de syntaxe attendue :\n");
            sb.append("const request = require('supertest');\n");
            sb.append("const app = require('../projet-reference/app');\n");
            sb.append("test('cas nominal - <description>', async () => {\n");
            sb.append("  const response = await request(app).put('/users/1').send({ statut: 'actif' });\n");
            sb.append("  expect(response.statusCode).toBe(200);\n");
            sb.append("  expect(response.body).toEqual({ statut: 'actif' });\n");
            sb.append("});\n");
            sb.append("NE redéfinis PAS le handler dans le test, utilise l'app existante.\n");
        } else {
            sb.append("IMPORTANT : le fichier de test doit etre autonome et directement executable. ");
            sb.append("Inclus la definition complete de la fonction directement dans le fichier de test ");
            sb.append("(ne fais AUCUN require ni import vers un fichier externe, puisque ce fichier n'existe pas).\n");
        }
        sb.append("Reponds uniquement avec du code Jest, sans texte explicatif ni balises markdown.");

        return sb.toString();
    }
    
    public String construirePromptBrut(UniteTestable unite, String extraitCodeSource) {
        StringBuilder sb = new StringBuilder();

        if (unite.isRoute()) {
            sb.append("Voici un handler de route Express : ")
              .append(unite.getMethode()).append(" ").append(unite.getChemin()).append(".\n\n");
        } else {
            sb.append("Voici une fonction JavaScript nommee ").append(unite.getNom()).append(".\n\n");
        }

        sb.append("Code source :\n").append(extraitCodeSource).append("\n");

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
            sb.append("IMPORTANT : Pour les routes Express, utilise OBLIGATOIREMENT Supertest avec l'application reelle. ");
            sb.append("Importe l'application depuis projet-reference/app.js qui contient le router monté.\n");
            sb.append("Exemple de syntaxe attendue :\n");
            sb.append("const request = require('supertest');\n");
            sb.append("const app = require('../projet-reference/app');\n");
            sb.append("test('cas nominal - <description>', async () => {\n");
            sb.append("  const response = await request(app).put('/users/1').send({ statut: 'actif' });\n");
            sb.append("  expect(response.statusCode).toBe(200);\n");
            sb.append("  expect(response.body).toEqual({ statut: 'actif' });\n");
            sb.append("});\n");
            sb.append("NE redéfinis PAS le handler dans le test, utilise l'app existante.\n");
        } else {
            sb.append("IMPORTANT : le fichier de test doit etre autonome et directement executable. ");
            sb.append("Inclus la definition complete de la fonction directement dans le fichier de test ");
            sb.append("(ne fais AUCUN require ni import vers un fichier externe, puisque ce fichier n'existe pas).\n");
        }
        sb.append("Reponds uniquement avec du code Jest, sans texte explicatif ni balises markdown.");

        return sb.toString();
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
    
}
