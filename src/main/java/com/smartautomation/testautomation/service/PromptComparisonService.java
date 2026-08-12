package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.exception.LlmCallException;
import com.smartautomation.testautomation.exception.TestGenerationException;
import com.smartautomation.testautomation.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PromptComparisonService {

    private static final Logger log = LoggerFactory.getLogger(PromptComparisonService.class);
    private static final Pattern PATTERN_NOMBRE_TESTS = Pattern.compile("(?m)^\\s*(test|it)\\s*\\(");

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir;

    @Value("${test.output-subdir:generated-tests}")
    private String outputSubdir;

    private final AstAnalysisService astAnalysisService;
    private final CodeSnippetExtractor snippetExtractor;
    private final PromptBuilderService promptBuilderService;
    private final LlmService llmService;
    private final TestCodeExtractor codeExtractor;
    private final JestExecutionService jestExecutionService;

    public PromptComparisonService(AstAnalysisService astAnalysisService,
                                    CodeSnippetExtractor snippetExtractor,
                                    PromptBuilderService promptBuilderService,
                                    LlmService llmService,
                                    TestCodeExtractor codeExtractor,
                                    JestExecutionService jestExecutionService) {
        this.astAnalysisService = astAnalysisService;
        this.snippetExtractor = snippetExtractor;
        this.promptBuilderService = promptBuilderService;
        this.llmService = llmService;
        this.codeExtractor = codeExtractor;
        this.jestExecutionService = jestExecutionService;
    }

    public PromptComparisonResult comparer(String cheminFichier, String nomUnite) throws IOException {
        AstAnalysisOutcome outcome = astAnalysisService.analyser(cheminFichier);
        if (outcome.isErreurTechnique()) {
            throw new IOException("Analyse AST impossible : " + outcome.getMessageErreur());
        }

        UniteTestable unite = outcome.getResultat().getUnites().stream()
                .filter(u -> nomUnite.equals(u.getNom()))
                .findFirst()
                .orElseThrow(() -> new IOException("Unite '" + nomUnite + "' introuvable"));

        String extrait = snippetExtractor.extraireExtrait(cheminFichier, unite.getLigneDebut(), unite.getLigneFin());

        List<String> cheminsAttendus = promptBuilderService.decrireCheminsACouvrirPublic(unite);

        // ---- Run 1 : prompt brut ----
        String promptBrut = promptBuilderService.construirePromptBrut(unite, extrait);
        RunResult resultatBrut = executerUnRun(promptBrut, nomUnite + "-brut");

        // ---- Run 2 : prompt structure ----
        String promptStructure = promptBuilderService.construirePromptCible(unite, extrait, List.of());
        RunResult resultatStructure = executerUnRun(promptStructure, nomUnite + "-structure");

        int occurrencesBrut = compterCheminsMentionnes(resultatBrut.codeGenere, cheminsAttendus);
        int occurrencesStructure = compterCheminsMentionnes(resultatStructure.codeGenere, cheminsAttendus);

        return new PromptComparisonResult(
                nomUnite, cheminsAttendus,
                resultatBrut.nombreTests, resultatBrut.testsReussis, occurrencesBrut,
                resultatStructure.nombreTests, resultatStructure.testsReussis, occurrencesStructure
        );
    }

    private RunResult executerUnRun(String prompt, String nomFichierTest) throws IOException {
        try {
            String reponseLlm = llmService.genererDepuisPrompt(prompt);
            String codeGenere = codeExtractor.extraireCode(reponseLlm);

            Path dossier = Path.of(scriptsDir, outputSubdir);
            if (!Files.exists(dossier)) Files.createDirectories(dossier);
            String nomFichier = nomFichierTest.replaceAll("[^a-zA-Z0-9_-]", "_").replaceAll("_+", "_") + ".test.js";
            Path fichier = dossier.resolve(nomFichier);
            Files.writeString(fichier, codeGenere, StandardCharsets.UTF_8);

            String cheminRelatif = outputSubdir + "/" + nomFichier;
            ExecutionTestResult resultatExec = jestExecutionService.executerTest(cheminRelatif);

            int nombreTests = compterOccurrences(codeGenere, PATTERN_NOMBRE_TESTS);
            int testsReussis = resultatExec.isErreurTechnique() ? 0 : resultatExec.getResultatJest().getTestsReussis();

            return new RunResult(codeGenere, nombreTests, testsReussis);

        } catch (LlmCallException | TestGenerationException e) {
            log.warn("Echec du run '{}' : {}", nomFichierTest, e.getMessage());
            throw new IOException("Erreur lors de la génération des tests pour " + nomFichierTest + " : " + e.getMessage(), e);
        }
    }

    private int compterOccurrences(String texte, Pattern pattern) {
        Matcher m = pattern.matcher(texte);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    private int compterCheminsMentionnes(String codeGenere, List<String> cheminsAttendus) {
        if (codeGenere == null || codeGenere.isBlank()) return 0;
        String codeMinuscule = codeGenere.toLowerCase(Locale.ROOT);
        int count = 0;
        for (String chemin : cheminsAttendus) {
            if (cheminEstCouvert(chemin, codeMinuscule)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Heuristique de detection de couverture d'un chemin dans le code de test genere.
     * Reste une heuristique textuelle (pas d'analyse semantique), mais couvre desormais
     * les principaux types de chemins produits par PromptBuilderService.decrireCheminsACouvrir :
     * nominal, erreurs HTTP, switch, try/catch, throw, et branches if/else.
     */
    private boolean cheminEstCouvert(String chemin, String codeMinuscule) {
        String cheminMinuscule = chemin.toLowerCase(Locale.ROOT);

        // Cas d'erreur HTTP : le code (ex. 400, 404) doit apparaitre dans le code genere
        Matcher mHttp = Pattern.compile("\\b(\\d{3})\\b").matcher(chemin);
        if (mHttp.find()) {
            return codeMinuscule.contains(mHttp.group(1));
        }

        // Cas nominal
        if (cheminMinuscule.contains("nominal")) {
            return codeMinuscule.contains("nominal");
        }

        // Cas d'un switch : recherche de mots-cles associes a un test de cas
        if (cheminMinuscule.contains("switch")) {
            return codeMinuscule.contains("switch")
                    || codeMinuscule.contains("case ")
                    || Pattern.compile("\\bcas\\s*\\d").matcher(codeMinuscule).find();
        }

        // Exception levee dans un bloc try/catch
        if (cheminMinuscule.contains("try") || cheminMinuscule.contains("exception")) {
            return codeMinuscule.contains("tothrow")
                    || codeMinuscule.contains(".rejects")
                    || codeMinuscule.contains("catch(");
        }

        // Erreur explicitement levee via throw
        if (cheminMinuscule.contains("throw")) {
            return codeMinuscule.contains("tothrow")
                    || codeMinuscule.contains(".rejects");
        }

        // Branche if vraie / fausse (else)
        if (cheminMinuscule.contains("condition if")) {
            if (cheminMinuscule.contains("fausse")) {
                return codeMinuscule.contains("else")
                        || codeMinuscule.contains("faux")
                        || codeMinuscule.contains("false")
                        || codeMinuscule.contains("invalide");
            }
            return codeMinuscule.contains("valide")
                    || codeMinuscule.contains("true")
                    || codeMinuscule.contains("vrai");
        }

        return false;
    }

    private record RunResult(String codeGenere, int nombreTests, int testsReussis) {}
}
