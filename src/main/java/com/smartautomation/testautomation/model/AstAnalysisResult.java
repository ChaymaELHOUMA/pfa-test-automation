package com.smartautomation.testautomation.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AstAnalysisResult {

    private String fichier;
    private List<UniteTestable> unites;

    public String getFichier() {
        return fichier;
    }

    public void setFichier(String fichier) {
        this.fichier = fichier;
    }

    public List<UniteTestable> getUnites() {
        return unites;
    }

    public void setUnites(List<UniteTestable> unites) {
        this.unites = unites;
    }
}
