package com.smartautomation.testautomation.model;

import java.util.List;
import java.util.Map;

public class MultiTypeTestResult {

    private final int totalTests;
    private final int testsReussis;
    private final int testsEchoues;
    private final double tauxSucces;
    private final Map<String, CategorieMetrics> resultatsParType;
    private final List<FichierDetail> detailsFichiers;

    public MultiTypeTestResult(int totalTests,
                               int testsReussis,
                               int testsEchoues,
                               double tauxSucces,
                               Map<String, CategorieMetrics> resultatsParType,
                               List<FichierDetail> detailsFichiers) {
        this.totalTests = totalTests;
        this.testsReussis = testsReussis;
        this.testsEchoues = testsEchoues;
        this.tauxSucces = tauxSucces;
        this.resultatsParType = resultatsParType != null ? resultatsParType : Map.of();
        this.detailsFichiers = detailsFichiers != null ? detailsFichiers : List.of();
    }

    public int getTotalTests() {
        return totalTests;
    }

    public int getTestsReussis() {
        return testsReussis;
    }

    public int getTestsEchoues() {
        return testsEchoues;
    }

    public double getTauxSucces() {
        return tauxSucces;
    }

    public Map<String, CategorieMetrics> getResultatsParType() {
        return resultatsParType;
    }

    public List<FichierDetail> getDetailsFichiers() {
        return detailsFichiers;
    }

    public record CategorieMetrics(
            int total,
            int reussis,
            int echoues,
            double tauxSucces
    ) {}

    public record FichierDetail(
            String nomFichier,
            String categorie,
            int totalTests,
            int testsReussis,
            int testsEchoues,
            boolean succes,
            String messageErreur
    ) {}
}
