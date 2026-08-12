package com.smartautomation.testautomation.model;

import java.util.List;

public class PromptComparisonResult {

    private final String nomUnite;
    private final List<String> cheminsAttendus;

    private final int nombreTestsGeneresBrut;
    private final int testsReussisBrut;
    private final int occurrencesCheminsMentionnesBrut;

    private final int nombreTestsGeneresStructure;
    private final int testsReussisStructure;
    private final int occurrencesCheminsMentionnesStructure;

    public PromptComparisonResult(String nomUnite, List<String> cheminsAttendus,
                                   int nombreTestsGeneresBrut, int testsReussisBrut, int occurrencesCheminsMentionnesBrut,
                                   int nombreTestsGeneresStructure, int testsReussisStructure, int occurrencesCheminsMentionnesStructure) {
        this.nomUnite = nomUnite;
        this.cheminsAttendus = cheminsAttendus;
        this.nombreTestsGeneresBrut = nombreTestsGeneresBrut;
        this.testsReussisBrut = testsReussisBrut;
        this.occurrencesCheminsMentionnesBrut = occurrencesCheminsMentionnesBrut;
        this.nombreTestsGeneresStructure = nombreTestsGeneresStructure;
        this.testsReussisStructure = testsReussisStructure;
        this.occurrencesCheminsMentionnesStructure = occurrencesCheminsMentionnesStructure;
    }

    public String getNomUnite() { return nomUnite; }
    public List<String> getCheminsAttendus() { return cheminsAttendus; }
    public int getNombreTestsGeneresBrut() { return nombreTestsGeneresBrut; }
    public int getTestsReussisBrut() { return testsReussisBrut; }
    public int getOccurrencesCheminsMentionnesBrut() { return occurrencesCheminsMentionnesBrut; }
    public int getNombreTestsGeneresStructure() { return nombreTestsGeneresStructure; }
    public int getTestsReussisStructure() { return testsReussisStructure; }
    public int getOccurrencesCheminsMentionnesStructure() { return occurrencesCheminsMentionnesStructure; }
}