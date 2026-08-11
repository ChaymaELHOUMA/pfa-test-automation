package com.smartautomation.testautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class BrancheInfo {

    private String type; // "if" ou "switch"
    private Boolean aUnElse;
    private Integer nombreCas;
    private int ligne;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getAUnElse() {
        return aUnElse;
    }

    public void setAUnElse(Boolean aUnElse) {
        this.aUnElse = aUnElse;
    }

    public Integer getNombreCas() {
        return nombreCas;
    }

    public void setNombreCas(Integer nombreCas) {
        this.nombreCas = nombreCas;
    }

    public int getLigne() {
        return ligne;
    }

    public void setLigne(int ligne) {
        this.ligne = ligne;
    }
}

