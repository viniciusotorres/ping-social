package com.pingsocial.config;

import com.pingsocial.exception.InvalidTokenException;
import com.pingsocial.models.StompPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final JwtTokenService jwtService;

    public JwtHandshakeInterceptor(JwtTokenService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        Optional<String> tokenOpt = extractToken(request);

        if (tokenOpt.isEmpty()) {
            logger.warn("Token JWT não fornecido na requisição WebSocket");
            return false;
        }

        String token = tokenOpt.get();

        try {
            String username = jwtService.getSubjectFromToken(token);
            attributes.put("principal", new StompPrincipal(username));
            logger.debug("Usuário autenticado para WebSocket: {}", username);
            return true;
        } catch (InvalidTokenException ex) {
            logger.warn("Token JWT inválido: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            logger.error("Erro inesperado ao validar token JWT: ", ex);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Pode ser usado para limpar recursos ou logging
    }

    /**
     * Extrai o token JWT do parâmetro "token" na requisição HTTP.
     */
    private Optional<String> extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getParameter("token");
            if (token != null && !token.isBlank()) {
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }
}
