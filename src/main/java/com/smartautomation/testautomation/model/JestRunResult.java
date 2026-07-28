package com.smartautomation.testautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JestRunResult {

    private boolean success;

    @JsonProperty("numTotalTests")
    private int totalTests;

    @JsonProperty("numPassedTests")
    private int testsReussis;

    @JsonProperty("numFailedTests")
    private int testsEchoues;

    private List<TestSuiteResult> testResults;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    public int getTestsReussis() {
        return testsReussis;
    }

    public void setTestsReussis(int testsReussis) {
        this.testsReussis = testsReussis;
    }

    public int getTestsEchoues() {
        return testsEchoues;
    }

    public void setTestsEchoues(int testsEchoues) {
        this.testsEchoues = testsEchoues;
    }

    public List<TestSuiteResult> getTestResults() {
        return testResults;
    }

    public void setTestResults(List<TestSuiteResult> testResults) {
        this.testResults = testResults;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TestSuiteResult {
        private String name;
        private String status;
        private List<AssertionResult> assertionResults;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<AssertionResult> getAssertionResults() {
            return assertionResults;
        }

        public void setAssertionResults(List<AssertionResult> assertionResults) {
            this.assertionResults = assertionResults;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssertionResult {
        private String title;
        private String status;
        private List<String> failureMessages;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<String> getFailureMessages() {
            return failureMessages;
        }

        public void setFailureMessages(List<String> failureMessages) {
            this.failureMessages = failureMessages;
        }
    }
}
