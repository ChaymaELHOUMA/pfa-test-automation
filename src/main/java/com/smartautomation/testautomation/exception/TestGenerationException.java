package com.smartautomation.testautomation.exception;

public class TestGenerationException extends RuntimeException {

    public TestGenerationException(String message) {
        super(message);
    }

    public TestGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
