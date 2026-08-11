package com.smartautomation.testautomation.model;


public class AstAnalysisOutcome {

    private final boolean erreurTechnique;
    private final String messageErreur;
    private final AstAnalysisResult resultat;

    private AstAnalysisOutcome(boolean erreurTechnique, String messageErreur, AstAnalysisResult resultat) {
        this.erreurTechnique = erreurTechnique;
        this.messageErreur = messageErreur;
        this.resultat = resultat;
    }

    public static AstAnalysisOutcome succes(AstAnalysisResult resultat) {
        return new AstAnalysisOutcome(false, null, resultat);
    }

    public static AstAnalysisOutcome echec(String messageErreur) {
        return new AstAnalysisOutcome(true, messageErreur, null);
    }

    public boolean isErreurTechnique() {
        return erreurTechnique;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public AstAnalysisResult getResultat() {
        return resultat;
    }
}
