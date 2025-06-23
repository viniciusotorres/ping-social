package com.pingsocial.exception;

/**
 * Exceção lançada quando ocorre um erro ao enviar um email.
 */
public class EmailSendingException extends RuntimeException {
    
    /**
     * Cria uma nova instância de EmailSendingException.
     *
     * @param message Mensagem de erro
     */
    public EmailSendingException(String message) {
        super(message);
    }
    
    /**
     * Cria uma nova instância de EmailSendingException.
     *
     * @param message Mensagem de erro
     * @param cause Causa raiz da exceção
     */
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}