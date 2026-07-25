package com.smartautomation.testautomation.model;

public class NodeExecutionResult {

    private final boolean success;
    private final String stdout;
    private final String stderr;
    private final int exitCode;
    private final boolean timedOut;

    public NodeExecutionResult(boolean success, String stdout, String stderr, int exitCode, boolean timedOut) {
        this.success = success;
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
        this.timedOut = timedOut;
    }

    public static NodeExecutionResult ok(String stdout, String stderr, int exitCode) {
        return new NodeExecutionResult(true, stdout, stderr, exitCode, false);
    }

    public static NodeExecutionResult echecTimeout() {
        return new NodeExecutionResult(false, "", "Timeout depasse : le process Node n'a pas repondu a temps", -1, true);
    }

    public static NodeExecutionResult echecExitCode(String stdout, String stderr, int exitCode) {
        return new NodeExecutionResult(false, stdout, stderr, exitCode, false);
    }

    public static NodeExecutionResult echecTechnique(String messageErreur) {
        return new NodeExecutionResult(false, "", messageErreur, -1, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    @Override
    public String toString() {
        return "NodeExecutionResult{" +
                "success=" + success +
                ", exitCode=" + exitCode +
                ", timedOut=" + timedOut +
                ", stdout='" + truncate(stdout) + '\'' +
                ", stderr='" + truncate(stderr) + '\'' +
                '}';
    }

    private String truncate(String s) {
        if (s == null) return "";
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }
}
