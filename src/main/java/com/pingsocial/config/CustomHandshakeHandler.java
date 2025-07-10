package com.pingsocial.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomHandshakeHandler.class);

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        Optional<Principal> optionalPrincipal = Optional.ofNullable((Principal) attributes.get("principal"));

        if (optionalPrincipal.isEmpty()) {
            logger.warn("Tentativa de handshake sem principal definido nos atributos.");
        } else {
            logger.debug("Handshake atribuído ao usuário: {}", optionalPrincipal.get().getName());
        }

        return optionalPrincipal.orElse(null);
    }
}
