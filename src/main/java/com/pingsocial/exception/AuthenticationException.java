package com.pingsocial.exception;

/**
 * Exceção lançada quando ocorre um erro durante o processo de autenticação.
 */
public class AuthenticationException extends RuntimeException {
    
    private final String email;
    
    /**
     * Cria uma nova instância de AuthenticationException.
     *
     * @param message Mensagem de erro
     * @param email Email do usuário que tentou autenticar
     */
    public AuthenticationException(String message, String email) {
        super(message);
        this.email = email;
    }
    
    /**
     * Retorna o email do usuário que tentou autenticar.
     *
     * @return Email do usuário
     */
    public String getEmail() {
        return email;
    }
}