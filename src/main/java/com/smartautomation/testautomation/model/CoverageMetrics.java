package com.smartautomation.testautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoverageMetrics {

    private int total;
    private int covered;
    private int skipped;
    private double pct;

    public CoverageMetrics() {
    }

    public CoverageMetrics(int total, int covered, int skipped, double pct) {
        this.total = total;
        this.covered = covered;
        this.skipped = skipped;
        this.pct = pct;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCovered() {
        return covered;
    }

    public void setCovered(int covered) {
        this.covered = covered;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public double getPct() {
        return pct;
    }

    public void setPct(double pct) {
        this.pct = pct;
    }
}
