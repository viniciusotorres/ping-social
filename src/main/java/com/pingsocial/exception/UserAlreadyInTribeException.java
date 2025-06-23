package com.pingsocial.exception;

public class UserAlreadyInTribeException extends RuntimeException {
    
    public UserAlreadyInTribeException(String message) {
        super(message);
    }
    
    public UserAlreadyInTribeException(Long userId, Long tribeId) {
        super("Usuário com ID: " + userId + " já é membro da tribo com ID: " + tribeId);
    }
}