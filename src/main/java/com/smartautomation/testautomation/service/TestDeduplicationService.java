package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.TestDeduplicationResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TestDeduplicationService {

    // Pattern pour extraire les titres de test
    private static final Pattern PATTERN_TEST_TITLE = Pattern.compile("(?m)^\\s*(?:test|it)\\s*\\(\\s*['\"]([^'\"]+)['\"]");

    /**
     * Extrait la liste des titres de tests présent dans un code JavaScript/Jest.
     */
    public List<String> extraireTitresTests(String codeTest) {
        if (codeTest == null || codeTest.isBlank()) {
            return List.of();
        }
        List<String> titres = new ArrayList<>();
        Matcher matcher = PATTERN_TEST_TITLE.matcher(codeTest);
        while (matcher.find()) {
            titres.add(matcher.group(1));
        }
        return titres;
    }

    /**
     * Analyse un code source Jest, détecte les doublons (par titre ou par corps de test)
     * et retourne un résultat contenant les métriques et le code nettoyé.
     */
    public TestDeduplicationResult analyserEtDedupliquer(String codeTest) {
        if (codeTest == null || codeTest.isBlank()) {
            return new TestDeduplicationResult(0, 0, 0, List.of(), true, codeTest);
        }

        List<BlocTestInfo> blocs = extraireBlocsTest(codeTest);
        if (blocs.isEmpty()) {
            return new TestDeduplicationResult(0, 0, 0, List.of(), true, codeTest);
        }

        Set<String> titresVusNormalises = new HashSet<>();
        Set<String> corpsVusNormalises = new HashSet<>();
        List<String> titresDoublons = new ArrayList<>();
        List<BlocTestInfo> blocsUniques = new ArrayList<>();

        for (BlocTestInfo bloc : blocs) {
            String titreNorm = normaliserTexte(bloc.titre);
            String corpsNorm = normaliserTexte(bloc.corps);

            boolean doublonTitre = titresVusNormalises.contains(titreNorm);
            boolean doublonCorps = !corpsNorm.isEmpty() && corpsVusNormalises.contains(corpsNorm);

            if (doublonTitre || doublonCorps) {
                titresDoublons.add(bloc.titre);
            } else {
                titresVusNormalises.add(titreNorm);
                if (!corpsNorm.isEmpty()) {
                    corpsVusNormalises.add(corpsNorm);
                }
                blocsUniques.add(bloc);
            }
        }

        int totalTests = blocs.size();
        int doublonsCount = titresDoublons.size();
        int testsUniques = blocsUniques.size();
        boolean estNonRedondant = (doublonsCount == 0);

        String codeDeduplique = reconstruireCode(codeTest, blocs, blocsUniques);

        return new TestDeduplicationResult(
                totalTests,
                testsUniques,
                doublonsCount,
                titresDoublons,
                estNonRedondant,
                codeDeduplique
        );
    }

    private List<BlocTestInfo> extraireBlocsTest(String code) {
        List<BlocTestInfo> blocs = new ArrayList<>();
        Matcher matcher = PATTERN_TEST_TITLE.matcher(code);

        while (matcher.find()) {
            String titre = matcher.group(1);
            int startIdx = matcher.start();
            int endIdx = trouverFinBlocTest(code, startIdx);

            String blocComplet = code.substring(startIdx, endIdx);
            String corps = extraireCorpsBloc(blocComplet);

            blocs.add(new BlocTestInfo(titre, startIdx, endIdx, blocComplet, corps));
        }

        return blocs;
    }

    private int trouverFinBlocTest(String code, int startIdx) {
        int len = code.length();
        int index = startIdx;

        // Avancer jusqu'à la première parenthèse ouvrante test(...)
        while (index < len && code.charAt(index) != '(') {
            index++;
        }

        if (index >= len) return len;

        int depthParen = 0;
        int depthBrace = 0;
        boolean inString = false;
        char stringChar = 0;

        for (; index < len; index++) {
            char c = code.charAt(index);

            if (inString) {
                if (c == stringChar && code.charAt(index - 1) != '\\') {
                    inString = false;
                }
                continue;
            }

            if (c == '\'' || c == '"' || c == '`') {
                inString = true;
                stringChar = c;
                continue;
            }

            if (c == '(') depthParen++;
            else if (c == ')') {
                depthParen--;
                if (depthParen == 0 && depthBrace == 0) {
                    index++;
                    // inclure le point-virgule éventuel qui suit
                    if (index < len && code.charAt(index) == ';') {
                        index++;
                    }
                    return index;
                }
            } else if (c == '{') depthBrace++;
            else if (c == '}') depthBrace--;
        }

        return len;
    }

    private String extraireCorpsBloc(String blocComplet) {
        int idxBrace = blocComplet.indexOf('{');
        int lastBrace = blocComplet.lastIndexOf('}');
        if (idxBrace != -1 && lastBrace > idxBrace) {
            return blocComplet.substring(idxBrace + 1, lastBrace);
        }
        return blocComplet;
    }

    private String normaliserTexte(String input) {
        if (input == null) return "";
        return input.toLowerCase(Locale.ROOT)
                .replaceAll("//.*", "")
                .replaceAll("/\\*.*?\\*/", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String reconstruireCode(String codeOriginal, List<BlocTestInfo> tousLesBlocs, List<BlocTestInfo> blocsUniques) {
        Set<BlocTestInfo> aConserver = new HashSet<>(blocsUniques);
        StringBuilder sb = new StringBuilder();
        int lastIndex = 0;

        for (BlocTestInfo bloc : tousLesBlocs) {
            sb.append(codeOriginal, lastIndex, bloc.startIdx);
            if (aConserver.contains(bloc)) {
                sb.append(bloc.blocComplet);
            } else {
                // Supprimer les saut de lignes inutiles laissés par la suppression du bloc
                while (sb.length() > 0 && (sb.charAt(sb.length() - 1) == '\n' || sb.charAt(sb.length() - 1) == '\r')) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append("\n");
            }
            lastIndex = bloc.endIdx;
        }
        sb.append(codeOriginal.substring(lastIndex));
        return sb.toString();
    }

    private static class BlocTestInfo {
        final String titre;
        final int startIdx;
        final int endIdx;
        final String blocComplet;
        final String corps;

        BlocTestInfo(String titre, int startIdx, int endIdx, String blocComplet, String corps) {
            this.titre = titre;
            this.startIdx = startIdx;
            this.endIdx = endIdx;
            this.blocComplet = blocComplet;
            this.corps = corps;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BlocTestInfo that)) return false;
            return startIdx == that.startIdx && endIdx == that.endIdx;
        }

        @Override
        public int hashCode() {
            return Objects.hash(startIdx, endIdx);
        }
    }
}
