package com.smartautomation.testautomation.model;

public class ExecutionTestResult {

    private final boolean erreurTechnique;
    private final boolean erreurCompilation;
    private final String messageErreurTechnique;
    /** Trace complète (stderr) renvoyée par node --check en cas d'erreur de compilation. */
    private final String detailErreurCompilation;
    private final JestRunResult resultatJest;

    private ExecutionTestResult(boolean erreurTechnique,
                                 boolean erreurCompilation,
                                 String messageErreurTechnique,
                                 String detailErreurCompilation,
                                 JestRunResult resultatJest) {
        this.erreurTechnique = erreurTechnique;
        this.erreurCompilation = erreurCompilation;
        this.messageErreurTechnique = messageErreurTechnique;
        this.detailErreurCompilation = detailErreurCompilation;
        this.resultatJest = resultatJest;
    }

    public static ExecutionTestResult depuisJest(JestRunResult resultatJest) {
        return new ExecutionTestResult(false, false, null, null, resultatJest);
    }

    public static ExecutionTestResult erreurTechnique(String message) {
        return new ExecutionTestResult(true, false, message, null, null);
    }

    /**
     * Crée un résultat représentant une erreur de syntaxe / compilation JS détectée
     * avant même l'exécution de Jest (via {@code node --check}).
     *
     * @param message message court destiné à l'utilisateur
     * @param detail  trace complète stderr de node --check
     */
    public static ExecutionTestResult erreurCompilation(String message, String detail) {
        return new ExecutionTestResult(true, true, message, detail, null);
    }

    public boolean isErreurTechnique() {
        return erreurTechnique;
    }

    public boolean isErreurCompilation() {
        return erreurCompilation;
    }

    public String getMessageErreurTechnique() {
        return messageErreurTechnique;
    }

    public String getDetailErreurCompilation() {
        return detailErreurCompilation;
    }

    public JestRunResult getResultatJest() {
        return resultatJest;
    }

    public boolean isTousTestsReussis() {
        return !erreurTechnique && resultatJest != null && resultatJest.isSuccess();
    }
}