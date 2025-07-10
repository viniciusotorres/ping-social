package com.pingsocial.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Token JWT inválido ou expirado.");
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
