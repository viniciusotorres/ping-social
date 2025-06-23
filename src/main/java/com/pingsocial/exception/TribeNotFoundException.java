package com.pingsocial.exception;

public class TribeNotFoundException extends RuntimeException {
    
    public TribeNotFoundException(String message) {
        super(message);
    }
    
    public TribeNotFoundException(Long tribeId) {
        super("Tribo não encontrada com ID: " + tribeId);
    }
}