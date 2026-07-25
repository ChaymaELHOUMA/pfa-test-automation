package com.smartautomation.testautomation.model;


public class ResultatGeneration {

    private final boolean succes;
    private final String cheminFichier;
    private final String messageErreur;

    private ResultatGeneration(boolean succes, String cheminFichier, String messageErreur) {
        this.succes = succes;
        this.cheminFichier = cheminFichier;
        this.messageErreur = messageErreur;
    }

    public static ResultatGeneration succes(String cheminFichier) {
        return new ResultatGeneration(true, cheminFichier, null);
    }

    public static ResultatGeneration echec(String messageErreur) {
        return new ResultatGeneration(false, null, messageErreur);
    }

    public boolean isSucces() {
        return succes;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public String getMessageErreur() {
        return messageErreur;
    }
}
