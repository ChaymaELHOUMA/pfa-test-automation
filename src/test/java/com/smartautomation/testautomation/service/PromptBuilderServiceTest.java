package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.BrancheInfo;
import com.smartautomation.testautomation.model.CasErreurInfo;
import com.smartautomation.testautomation.model.UniteTestable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PromptBuilderServiceTest {

    private PromptBuilderService promptBuilderService;
    private ProjectService projectService;

    @BeforeEach
    public void setUp() {
        projectService = new ProjectService();
        projectService.init();
        promptBuilderService = new PromptBuilderService(projectService);
    }

    @Test
    public void testConstruirePromptCible_FonctionJs() {
        UniteTestable unite = new UniteTestable();
        unite.setType("fonction");
        unite.setNom("validerEmail");
        unite.setParametres(List.of("email"));

        BrancheInfo branche = new BrancheInfo();
        branche.setType("if");
        branche.setLigne(7);
        branche.setAUnElse(false);
        unite.setBranches(List.of(branche));

        String extrait = "function validerEmail(email) { return /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/.test(email); }";

        String prompt = promptBuilderService.construirePromptCible(unite, extrait, List.of());

        assertNotNull(prompt);
        assertTrue(prompt.contains("validerEmail"));
        assertTrue(prompt.contains("FORMAT DE NOMMAGE STRICTEMENT OBLIGATOIRE"));
        assertTrue(prompt.contains("cas nominal - <description>"));
        assertTrue(prompt.contains("expect(() => maFonction(...)).toThrow()"));
        assertTrue(prompt.contains("IMPORTANT POUR FONCTION STANDALONE"));
    }

    @Test
    public void testConstruirePromptCible_RouteExpress() {
        UniteTestable unite = new UniteTestable();
        unite.setType("route");
        unite.setNom("PUT /users/:id");
        unite.setMethode("PUT");
        unite.setChemin("/users/:id");
        unite.setParametres(List.of("req", "res"));

        CasErreurInfo casErreur = new CasErreurInfo();
        casErreur.setType("reponse-erreur-http");
        casErreur.setLigne(47);
        casErreur.setCodeHttp(400);
        unite.setCasErreur(List.of(casErreur));

        String extrait = "router.put('/users/:id', (req, res) => { ... });";

        String prompt = promptBuilderService.construirePromptCible(unite, extrait, List.of());

        assertNotNull(prompt);
        assertTrue(prompt.contains("IMPORTANT POUR ROUTE EXPRESS (SUPERTEST)"));
        assertTrue(prompt.contains("require('../projet-reference/app')"));
        assertTrue(prompt.contains("require('../projet-reference/utilisateurRepository')"));
        assertTrue(prompt.contains("cas erreur - <description>"));
    }

    @Test
    public void testConstruirePromptIntegration() {
        UniteTestable unite = new UniteTestable();
        unite.setType("route");
        unite.setNom("PUT /users/:id");
        unite.setMethode("PUT");
        unite.setChemin("/users/:id");

        String extrait = "router.put('/users/:id', ...);";
        String contexte = "--- Service ---\nclass Service {...}";

        String prompt = promptBuilderService.construirePromptIntegration(unite, extrait, contexte);

        assertNotNull(prompt);
        assertTrue(prompt.contains("VERIFICATION DE PERSISTANCE"));
        assertTrue(prompt.contains("integration - <description>"));
        assertTrue(prompt.contains("const app = require('../projet-reference/app');"));
    }
}
