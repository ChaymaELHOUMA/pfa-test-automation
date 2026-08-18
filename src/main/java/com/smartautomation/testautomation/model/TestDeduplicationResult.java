package com.smartautomation.testautomation.model;

import java.util.List;

public class TestDeduplicationResult {
    private final int totalTests;
    private final int testsUniques;
    private final int doublonsDetectes;
    private final List<String> titresDoublons;
    private final boolean estNonRedondant;
    private final String codeDeduplique;

    public TestDeduplicationResult(int totalTests,
                                   int testsUniques,
                                   int doublonsDetectes,
                                   List<String> titresDoublons,
                                   boolean estNonRedondant,
                                   String codeDeduplique) {
        this.totalTests = totalTests;
        this.testsUniques = testsUniques;
        this.doublonsDetectes = doublonsDetectes;
        this.titresDoublons = titresDoublons != null ? titresDoublons : List.of();
        this.estNonRedondant = estNonRedondant;
        this.codeDeduplique = codeDeduplique;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public int getTestsUniques() {
        return testsUniques;
    }

    public int getDoublonsDetectes() {
        return doublonsDetectes;
    }

    public List<String> getTitresDoublons() {
        return titresDoublons;
    }

    public boolean isEstNonRedondant() {
        return estNonRedondant;
    }

    public String getCodeDeduplique() {
        return codeDeduplique;
    }
}
