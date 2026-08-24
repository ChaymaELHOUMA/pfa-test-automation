package com.smartautomation.testautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MutationResult {

    private boolean succes;
    private String messageErreur;
    private MutantMetrics metrics;
    private Map<String, MutantMetrics> metricsParFichier;

    public MutationResult() {
    }

    public static MutationResult succes(MutantMetrics metrics, Map<String, MutantMetrics> metricsParFichier) {
        MutationResult result = new MutationResult();
        result.succes = true;
        result.metrics = metrics;
        result.metricsParFichier = metricsParFichier;
        return result;
    }

    public static MutationResult erreur(String messageErreur) {
        MutationResult result = new MutationResult();
        result.succes = false;
        result.messageErreur = messageErreur;
        return result;
    }

    public boolean isSucces() {
        return succes;
    }

    public void setSucces(boolean succes) {
        this.succes = succes;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
    }

    public MutantMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(MutantMetrics metrics) {
        this.metrics = metrics;
    }

    public Map<String, MutantMetrics> getMetricsParFichier() {
        return metricsParFichier;
    }

    public void setMetricsParFichier(Map<String, MutantMetrics> metricsParFichier) {
        this.metricsParFichier = metricsParFichier;
    }
}
