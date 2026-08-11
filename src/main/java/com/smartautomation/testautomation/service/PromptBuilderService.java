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

        sb.append("Genere des tests Jest");
        if (unite.isRoute()) {
            sb.append(" (avec Supertest si pertinent)");
        }
        sb.append(" couvrant chacun des chemins listes ci-dessus.\n");
        sb.append("IMPORTANT : le fichier de test doit etre autonome et directement executable. ");
        sb.append("Inclus la definition complete de la fonction ou du handler directement dans le fichier de test ");
        sb.append("(ne fais AUCUN require ni import vers un fichier externe, puisque ce fichier n'existe pas).\n");
        sb.append("Reponds uniquement avec un bloc de code JavaScript delimite par ```javascript et ```, ");
        sb.append("sans aucune explication avant ou apres le bloc.");

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
}
