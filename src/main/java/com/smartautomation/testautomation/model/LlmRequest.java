package com.smartautomation.testautomation.model;

import java.util.List;

public class LlmRequest {

    private String model;
    private int maxTokens;
    private List<Message> messages;

    public LlmRequest() {
    }

    public LlmRequest(String model, int maxTokens, String promptUtilisateur) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.messages = List.of(new Message("user", promptUtilisateur));
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public static class Message {
        private String role;
        private String content;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
