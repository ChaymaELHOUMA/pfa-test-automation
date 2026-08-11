package com.smartautomation.testautomation.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class CodeSnippetExtractor {

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir;

    public String extraireExtrait(String cheminFichierRelatif, int ligneDebut, int ligneFin) throws IOException {
        Path fichier = Path.of(scriptsDir, cheminFichierRelatif);
        List<String> lignes = Files.readAllLines(fichier, StandardCharsets.UTF_8);

        int debut = Math.max(1, ligneDebut);
        int fin = Math.min(lignes.size(), ligneFin);

        StringBuilder sb = new StringBuilder();
        for (int i = debut; i <= fin; i++) {
            sb.append(lignes.get(i - 1)).append(System.lineSeparator());
        }
        return sb.toString();
    }
}