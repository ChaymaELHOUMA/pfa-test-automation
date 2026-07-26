package com.smartautomation.testautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmResponse {

    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public String getTexteComplet() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }
        Candidate premier = candidates.get(0);
        if (premier.getContent() == null || premier.getContent().getParts() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Part part : premier.getContent().getParts()) {
            if (part.getText() != null) {
                sb.append(part.getText());
            }
        }
        return sb.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
