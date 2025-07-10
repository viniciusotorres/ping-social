package com.pingsocial.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    private final JwtTokenService jwtService;

    public WebSocketConfig(JwtTokenService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        logger.info("Registrando endpoint WebSocket '/ws' com suporte a SockJS");

        registry.addEndpoint("/ws")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .addInterceptors(new JwtHandshakeInterceptor(jwtService))
                .setAllowedOriginPatterns(
                        "http://localhost:4200",
                        "https://ping-social-front-ym8d.vercel.app"
                )
                .withSockJS();

        logger.info("Handshake configurado com JwtInterceptor e CustomHandler.");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        logger.info("Configurando message broker: '/app' como prefixo de aplicação, '/topic' e '/queue' como destinos simples.");

        config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic", "/queue");
        config.setUserDestinationPrefix("/user");
    }
}
