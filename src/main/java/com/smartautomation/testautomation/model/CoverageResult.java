package com.smartautomation.testautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoverageResult {

    private boolean succes;
    private String messageErreur;

    private CoverageMetrics lines;
    private CoverageMetrics branches;
    private CoverageMetrics statements;
    private CoverageMetrics functions;

    private Map<String, FileCoverageSummary> detailsParFichier;

    public CoverageResult() {
    }

    public static CoverageResult succes(CoverageMetrics lines,
                                        CoverageMetrics branches,
                                        CoverageMetrics statements,
                                        CoverageMetrics functions,
                                        Map<String, FileCoverageSummary> detailsParFichier) {
        CoverageResult result = new CoverageResult();
        result.succes = true;
        result.lines = lines;
        result.branches = branches;
        result.statements = statements;
        result.functions = functions;
        result.detailsParFichier = detailsParFichier;
        return result;
    }

    public static CoverageResult erreur(String messageErreur) {
        CoverageResult result = new CoverageResult();
        result.succes = false;
        result.messageErreur = messageErreur;
        return result;
    }

    public boolean isSucces() {
        return succes;
    }

    public void setSucces(boolean succes) {
        this.succes = succes;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
    }

    public CoverageMetrics getLines() {
        return lines;
    }

    public void setLines(CoverageMetrics lines) {
        this.lines = lines;
    }

    public CoverageMetrics getBranches() {
        return branches;
    }

    public void setBranches(CoverageMetrics branches) {
        this.branches = branches;
    }

    public CoverageMetrics getStatements() {
        return statements;
    }

    public void setStatements(CoverageMetrics statements) {
        this.statements = statements;
    }

    public CoverageMetrics getFunctions() {
        return functions;
    }

    public void setFunctions(CoverageMetrics functions) {
        this.functions = functions;
    }

    public Map<String, FileCoverageSummary> getDetailsParFichier() {
        return detailsParFichier;
    }

    public void setDetailsParFichier(Map<String, FileCoverageSummary> detailsParFichier) {
        this.detailsParFichier = detailsParFichier;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileCoverageSummary {
        private CoverageMetrics lines;
        private CoverageMetrics branches;
        private CoverageMetrics statements;
        private CoverageMetrics functions;

        public FileCoverageSummary() {
        }

        public FileCoverageSummary(CoverageMetrics lines, CoverageMetrics branches, CoverageMetrics statements, CoverageMetrics functions) {
            this.lines = lines;
            this.branches = branches;
            this.statements = statements;
            this.functions = functions;
        }

        public CoverageMetrics getLines() {
            return lines;
        }

        public void setLines(CoverageMetrics lines) {
            this.lines = lines;
        }

        public CoverageMetrics getBranches() {
            return branches;
        }

        public void setBranches(CoverageMetrics branches) {
            this.branches = branches;
        }

        public CoverageMetrics getStatements() {
            return statements;
        }

        public void setStatements(CoverageMetrics statements) {
            this.statements = statements;
        }

        public CoverageMetrics getFunctions() {
            return functions;
        }

        public void setFunctions(CoverageMetrics functions) {
            this.functions = functions;
        }
    }
}
