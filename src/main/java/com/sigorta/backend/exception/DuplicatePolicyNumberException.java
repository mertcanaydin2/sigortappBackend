package com.sigorta.backend.exception;

public class DuplicatePolicyNumberException extends RuntimeException {

    public DuplicatePolicyNumberException(String message) {
        super(message);
    }
}
