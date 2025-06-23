package com.pingsocial.exception;

/**
 * Exceção lançada quando um código de validação inválido é fornecido.
 */
public class InvalidValidationCodeException extends RuntimeException {
    
    /**
     * Cria uma nova instância de InvalidValidationCodeException.
     *
     * @param message Mensagem de erro
     */
    public InvalidValidationCodeException(String message) {
        super(message);
    }
    
    /**
     * Cria uma nova instância de InvalidValidationCodeException.
     *
     * @param message Mensagem de erro
     * @param cause Causa raiz da exceção
     */
    public InvalidValidationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}