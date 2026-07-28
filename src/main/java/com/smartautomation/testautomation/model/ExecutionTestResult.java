package com.smartautomation.testautomation.model;

public class ExecutionTestResult {

    private final boolean erreurTechnique;
    private final String messageErreurTechnique;
    private final JestRunResult resultatJest;

    private ExecutionTestResult(boolean erreurTechnique, String messageErreurTechnique, JestRunResult resultatJest) {
        this.erreurTechnique = erreurTechnique;
        this.messageErreurTechnique = messageErreurTechnique;
        this.resultatJest = resultatJest;
    }

    public static ExecutionTestResult depuisJest(JestRunResult resultatJest) {
        return new ExecutionTestResult(false, null, resultatJest);
    }

    public static ExecutionTestResult erreurTechnique(String message) {
        return new ExecutionTestResult(true, message, null);
    }

    public boolean isErreurTechnique() {
        return erreurTechnique;
    }

    public String getMessageErreurTechnique() {
        return messageErreurTechnique;
    }

    public JestRunResult getResultatJest() {
        return resultatJest;
    }

    public boolean isTousTestsReussis() {
        return !erreurTechnique && resultatJest != null && resultatJest.isSuccess();
    }
}