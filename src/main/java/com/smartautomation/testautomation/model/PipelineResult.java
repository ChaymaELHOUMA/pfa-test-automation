package com.smartautomation.testautomation.model;

public class PipelineResult {

    private final boolean succes;
    private final String etapeEnEchec;
    private final String messageErreur;
    private final String cheminFichierTest;
    private final String codeTest;
    private final Integer totalTests;
    private final Integer testsReussis;
    private final Integer testsEchoues;

    private PipelineResult(boolean succes, String etapeEnEchec, String messageErreur,
                            String cheminFichierTest, String codeTest, Integer totalTests,
                            Integer testsReussis, Integer testsEchoues) {
        this.succes = succes;
        this.etapeEnEchec = etapeEnEchec;
        this.messageErreur = messageErreur;
        this.cheminFichierTest = cheminFichierTest;
        this.codeTest = codeTest;
        this.totalTests = totalTests;
        this.testsReussis = testsReussis;
        this.testsEchoues = testsEchoues;
    }

    public static PipelineResult echecGeneration(String messageErreur) {
        return new PipelineResult(false, "generation", messageErreur, null, null, null, null, null);
    }

    public static PipelineResult echecExecution(String cheminFichierTest, String messageErreur) {
        return new PipelineResult(false, "execution", messageErreur, cheminFichierTest, null, null, null, null);
    }

    public static PipelineResult echecExecution(String cheminFichierTest, String codeTest, String messageErreur) {
        return new PipelineResult(false, "execution", messageErreur, cheminFichierTest, codeTest, null, null, null);
    }

    public static PipelineResult succes(String cheminFichierTest, String codeTest, int totalTests, int testsReussis, int testsEchoues) {
        return new PipelineResult(true, null, null, cheminFichierTest, codeTest, totalTests, testsReussis, testsEchoues);
    }

    public static PipelineResult succes(String cheminFichierTest, int totalTests, int testsReussis, int testsEchoues) {
        return new PipelineResult(true, null, null, cheminFichierTest, null, totalTests, testsReussis, testsEchoues);
    }

    public boolean isSucces() {
        return succes;
    }

    public String getEtapeEnEchec() {
        return etapeEnEchec;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public String getCheminFichierTest() {
        return cheminFichierTest;
    }

    public String getCodeTest() {
        return codeTest;
    }

    public Integer getTotalTests() {
        return totalTests;
    }

    public Integer getTestsReussis() {
        return testsReussis;
    }

    public Integer getTestsEchoues() {
        return testsEchoues;
    }
}
