package com.smartautomation.testautomation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartautomation.testautomation.model.ExecutionTestResult;
import com.smartautomation.testautomation.model.JestRunResult;
import com.smartautomation.testautomation.model.NodeExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JestExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JestExecutionService.class);

    /** Clés JSON obligatoires dans un rapport Jest. Au moins une doit être présente. */
    private static final List<String> CLES_RAPPORT_JEST = List.of(
            "\"numTotalTests\"", "\"numFailedTestSuites\"", "\"testResults\"", "\"numTotalTestSuites\""
    );

    private final NodeBridgeService nodeBridgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jest.execution.timeout-seconds:45}")
    private long timeoutSeconds;

    public JestExecutionService(NodeBridgeService nodeBridgeService) {
        this.nodeBridgeService = nodeBridgeService;
    }

    public ExecutionTestResult executerTest(String cheminFichierTest) {
        LOGGER.info("Execution du test genere via Jest : {}", cheminFichierTest);

        // ── 1. Validation syntaxique préalable via node --check ─────────────────
        NodeExecutionResult syntaxCheck = verifierSyntaxe(cheminFichierTest);
        if (!syntaxCheck.isSuccess() && !syntaxCheck.isTimedOut()) {
            String erreurSyntaxe = syntaxCheck.getStderr();
            if (erreurSyntaxe != null && !erreurSyntaxe.isBlank()) {
                LOGGER.warn("Erreur de compilation/syntaxe dans {} : {}", cheminFichierTest, tronquer(erreurSyntaxe));
                return ExecutionTestResult.erreurCompilation(
                        "Le test genere ne compile pas : " + tronquer(erreurSyntaxe),
                        erreurSyntaxe
                );
            }
        }

        // ── 2. Exécution Jest avec --json ────────────────────────────────────────
        NodeExecutionResult resultatProcess = nodeBridgeService.runScriptAtPathWithTimeout(
                "node_modules/jest/bin/jest.js",
                timeoutSeconds,
                cheminFichierTest, "--json", "--ci", "--forceExit"
        );

        // ── 3. Parsing de la sortie Jest ─────────────────────────────────────────
        JestRunResult resultatJest = parserSortieJest(resultatProcess.getStdout(), resultatProcess.getStderr());
        if (resultatJest != null) {
            LOGGER.info("Resultat Jest pour {} : {}/{} tests reussis (success={}, timedOut={})",
                    cheminFichierTest,
                    resultatJest.getTestsReussis(),
                    resultatJest.getTotalTests(),
                    resultatJest.isSuccess(),
                    resultatProcess.isTimedOut());
            return ExecutionTestResult.depuisJest(resultatJest);
        }

        // ── 4. Cas de timeout ─────────────────────────────────────────────────────
        if (resultatProcess.isTimedOut()) {
            LOGGER.warn("Timeout lors de l execution du test {} (possible boucle infinie generee par le LLM)", cheminFichierTest);
            return ExecutionTestResult.erreurTechnique(
                    "Timeout depasse (" + timeoutSeconds + "s) : le test genere ne s est pas termine a temps");
        }

        // ── 5. Aucune sortie utilisable ───────────────────────────────────────────
        String stdout = resultatProcess.getStdout();
        String stderr = resultatProcess.getStderr();

        if (stdout == null || stdout.isBlank()) {
            LOGGER.error("Aucune sortie JSON recue de Jest pour {} : {}", cheminFichierTest, stderr);
            return ExecutionTestResult.erreurTechnique(
                    "Jest n a produit aucune sortie exploitable. stderr : " + tronquer(stderr));
        }

        LOGGER.error("Impossible de parser la sortie JSON de Jest pour {}", cheminFichierTest);
        // Fournir la première ligne d'erreur significative du stderr si disponible
        String detail = extrairePremiereErreur(stderr);
        if (detail == null || detail.isBlank()) {
            detail = tronquer(stdout);
        }
        return ExecutionTestResult.erreurTechnique(
                "Sortie de Jest illisible (pas un JSON valide) : " + detail);
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  Package-private pour les tests unitaires
    // ────────────────────────────────────────────────────────────────────────────

    JestRunResult parserSortieJest(String stdout, String stderr) {
        JestRunResult depuisStdout = parserJsonJest(extraireJsonJest(stdout));
        if (depuisStdout != null) {
            return depuisStdout;
        }
        return parserJsonJest(extraireJsonJest(stderr));
    }

    /**
     * Extrait le premier bloc JSON équilibré qui correspond à un rapport Jest.
     *
     * <p>Contrairement à la version précédente qui prenait le premier '{' et le dernier '}',
     * cette méthode parcourt la chaîne caractère par caractère en gérant :
     * <ul>
     *   <li>les chaînes entre guillemets (simples, doubles, backtick) et les séquences d'échappement ;</li>
     *   <li>l'imbrication des accolades afin de délimiter correctement le bloc JSON ;</li>
     *   <li>la validation que le bloc trouvé contient bien des clés propres à Jest.</li>
     * </ul>
     */
    String extraireJsonJest(String sortie) {
        if (sortie == null || sortie.isBlank()) {
            return null;
        }

        // Suppression BOM éventuel
        String texte = sortie.stripLeading();
        if (texte.startsWith("\uFEFF")) {
            texte = texte.substring(1);
        }

        List<String> candidats = extraireBlocs(texte);

        // Valider chaque candidat : il doit contenir au moins une clé Jest connue
        for (String candidat : candidats) {
            for (String cle : CLES_RAPPORT_JEST) {
                if (candidat.contains(cle)) {
                    return candidat;
                }
            }
        }

        // Aucun candidat valide → retourner null
        return null;
    }

    /**
     * Extrait tous les blocs JSON de premier niveau (accolades équilibrées) présents
     * dans {@code texte}. Gère les chaînes entre guillemets et les échappements.
     */
    List<String> extraireBlocs(String texte) {
        List<String> blocs = new ArrayList<>();
        int n = texte.length();
        int i = 0;
        while (i < n) {
            // Chercher le prochain '{'
            while (i < n && texte.charAt(i) != '{') {
                i++;
            }
            if (i >= n) break;

            int debut = i;
            int profondeur = 0;
            boolean dansChaine = false;
            char quoteChar = 0;

            while (i < n) {
                char c = texte.charAt(i);

                if (dansChaine) {
                    if (c == '\\') {
                        i += 2; // ignorer le caractère échappé
                        continue;
                    }
                    if (c == quoteChar) {
                        dansChaine = false;
                    }
                } else {
                    if (c == '"' || c == '\'' || c == '`') {
                        dansChaine = true;
                        quoteChar = c;
                    } else if (c == '{') {
                        profondeur++;
                    } else if (c == '}') {
                        profondeur--;
                        if (profondeur == 0) {
                            blocs.add(texte.substring(debut, i + 1));
                            i++;
                            break;
                        }
                    }
                }
                i++;
            }
            // Si profondeur > 0 ici, le bloc est malformé → avancer juste après le '{' initial
            if (profondeur > 0) {
                i = debut + 1;
            }
        }
        return blocs;
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  Méthodes privées
    // ────────────────────────────────────────────────────────────────────────────

    private JestRunResult parserJsonJest(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JestRunResult resultat = objectMapper.readValue(json, JestRunResult.class);
            enrichirComptages(resultat);
            return resultat;
        } catch (JsonProcessingException e) {
            LOGGER.debug("Flux Jest non parsable en JSON : {}", e.getMessage());
            return null;
        }
    }

    /**
     * Lance {@code node --check <chemin>} pour détecter une erreur de syntaxe JS
     * sans exécuter le fichier.
     */
    private NodeExecutionResult verifierSyntaxe(String cheminFichierTest) {
        return nodeBridgeService.runScriptAtPathWithTimeout(
                "--check", 10,
                cheminFichierTest
        );
    }

    /**
     * Enrichit les comptages du résultat Jest :
     * <ol>
     *   <li>Si les compteurs de tests sont à 0 mais qu'il y a des assertionResults, on
     *       recalcule depuis les assertions.</li>
     *   <li>Si des suites ont échoué (erreur de compilation / runtime) sans assertions,
     *       on comptabilise chaque suite échouée comme 1 test échoué.</li>
     * </ol>
     */
    private void enrichirComptages(JestRunResult resultat) {
        int reussisDepuisAssertions = compterAssertionsParStatut(resultat, "passed");
        int totalDepuisAssertions = compterTotalAssertions(resultat);

        if (resultat.getTestsReussis() == 0 && reussisDepuisAssertions > 0) {
            resultat.setTestsReussis(reussisDepuisAssertions);
        }
        if (resultat.getTotalTests() == 0 && totalDepuisAssertions > 0) {
            resultat.setTotalTests(totalDepuisAssertions);
        }

        // Suites en erreur de compilation/runtime sans tests (totalTests toujours 0)
        int suitesErreur = resultat.getSuiteEchouees() + resultat.getSuiteErreursRuntime();
        if (resultat.getTotalTests() == 0 && suitesErreur > 0) {
            resultat.setTotalTests(suitesErreur);
            resultat.setTestsEchoues(suitesErreur);
        }
    }

    private int compterAssertionsParStatut(JestRunResult resultat, String statutAttendu) {
        if (resultat.getTestResults() == null) {
            return 0;
        }
        int count = 0;
        for (JestRunResult.TestSuiteResult suite : resultat.getTestResults()) {
            if (suite.getAssertionResults() == null) {
                continue;
            }
            for (JestRunResult.AssertionResult assertion : suite.getAssertionResults()) {
                if (statutAttendu.equals(assertion.getStatus())) {
                    count++;
                }
            }
        }
        return count;
    }

    private int compterTotalAssertions(JestRunResult resultat) {
        if (resultat.getTestResults() == null) {
            return 0;
        }
        int count = 0;
        for (JestRunResult.TestSuiteResult suite : resultat.getTestResults()) {
            if (suite.getAssertionResults() != null) {
                count += suite.getAssertionResults().size();
            }
        }
        return count;
    }

    /**
     * Extrait la première ligne d'erreur significative du flux stderr (non vide,
     * ne commençant pas par {@code at } qui sont des lignes de stack trace).
     */
    private String extrairePremiereErreur(String stderr) {
        if (stderr == null || stderr.isBlank()) return null;
        for (String ligne : stderr.split("\\R")) {
            String l = ligne.strip();
            if (!l.isEmpty() && !l.startsWith("at ") && !l.startsWith("●")
                    && !l.startsWith("PASS") && !l.startsWith("FAIL")) {
                return tronquer(l);
            }
        }
        return null;
    }

    private String tronquer(String texte) {
        if (texte == null) return "";
        return texte.length() > 500 ? texte.substring(0, 500) + "..." : texte;
    }
}
