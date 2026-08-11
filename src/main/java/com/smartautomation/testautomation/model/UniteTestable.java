package com.smartautomation.testautomation.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UniteTestable {

    private String type; // "fonction" ou "route"
    private String nom;
    private String methode;  // uniquement pour type = "route"
    private String chemin;   // uniquement pour type = "route"
    private List<String> parametres;
    private int ligneDebut;
    private int ligneFin;
    private List<BrancheInfo> branches;
    private List<CasErreurInfo> casErreur;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getMethode() {
        return methode;
    }

    public void setMethode(String methode) {
        this.methode = methode;
    }

    public String getChemin() {
        return chemin;
    }

    public void setChemin(String chemin) {
        this.chemin = chemin;
    }

    public List<String> getParametres() {
        return parametres;
    }

    public void setParametres(List<String> parametres) {
        this.parametres = parametres;
    }

    public int getLigneDebut() {
        return ligneDebut;
    }

    public void setLigneDebut(int ligneDebut) {
        this.ligneDebut = ligneDebut;
    }

    public int getLigneFin() {
        return ligneFin;
    }

    public void setLigneFin(int ligneFin) {
        this.ligneFin = ligneFin;
    }

    public List<BrancheInfo> getBranches() {
        return branches;
    }

    public void setBranches(List<BrancheInfo> branches) {
        this.branches = branches;
    }

    public List<CasErreurInfo> getCasErreur() {
        return casErreur;
    }

    public void setCasErreur(List<CasErreurInfo> casErreur) {
        this.casErreur = casErreur;
    }

    public boolean isRoute() {
        return "route".equals(type);
    }

    public boolean isFonction() {
        return "fonction".equals(type);
    }
}
