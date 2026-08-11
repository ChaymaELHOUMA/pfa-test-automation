package com.smartautomation.testautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CasErreurInfo {

    private String type; // "reponse-erreur-http" | "try-catch" | "throw"
    private Integer codeHttp;
    private Boolean aUnFinally;
    private int ligne;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCodeHttp() {
        return codeHttp;
    }

    public void setCodeHttp(Integer codeHttp) {
        this.codeHttp = codeHttp;
    }

    public Boolean getAUnFinally() {
        return aUnFinally;
    }

    public void setAUnFinally(Boolean aUnFinally) {
        this.aUnFinally = aUnFinally;
    }

    public int getLigne() {
        return ligne;
    }

    public void setLigne(int ligne) {
        this.ligne = ligne;
    }
}
