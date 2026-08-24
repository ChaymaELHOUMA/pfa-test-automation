package com.smartautomation.testautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MutantMetrics {

    private int killed;
    private int survived;
    private int timedOut;
    private int noCoverage;
    private int compileError;
    private int total;
    private double scoreMutationPct;

    public MutantMetrics() {
    }

    public MutantMetrics(int killed, int survived, int timedOut, int noCoverage, int compileError, int total, double scoreMutationPct) {
        this.killed = killed;
        this.survived = survived;
        this.timedOut = timedOut;
        this.noCoverage = noCoverage;
        this.compileError = compileError;
        this.total = total;
        this.scoreMutationPct = scoreMutationPct;
    }

    public int getKilled() {
        return killed;
    }

    public void setKilled(int killed) {
        this.killed = killed;
    }

    public int getSurvived() {
        return survived;
    }

    public void setSurvived(int survived) {
        this.survived = survived;
    }

    public int getTimedOut() {
        return timedOut;
    }

    public void setTimedOut(int timedOut) {
        this.timedOut = timedOut;
    }

    public int getNoCoverage() {
        return noCoverage;
    }

    public void setNoCoverage(int noCoverage) {
        this.noCoverage = noCoverage;
    }

    public int getCompileError() {
        return compileError;
    }

    public void setCompileError(int compileError) {
        this.compileError = compileError;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public double getScoreMutationPct() {
        return scoreMutationPct;
    }

    public void setScoreMutationPct(double scoreMutationPct) {
        this.scoreMutationPct = scoreMutationPct;
    }
}
