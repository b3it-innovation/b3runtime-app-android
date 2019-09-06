package com.b3.development.b3runtime.utils.failure;

/**
 * A helper class to standardise error handling
 */
public class Failure {
    FailureType type = FailureType.GENERIC;
    String message = "";

    public Failure() {
    }

    public Failure(FailureType type, String message) {
        this.type = type;
        this.message = message;
    }

    public Failure(FailureType type) {
        this.type = type;
    }
}