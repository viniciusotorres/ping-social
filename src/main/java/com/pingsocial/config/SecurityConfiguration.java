package com.pingsocial.config;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração de segurança da aplicação.
 * Define as regras de autenticação e autorização para os endpoints da API.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final UserAuthenticationFilter userAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Construtor com injeção de dependências.
     *
     * @param userAuthenticationFilter Filtro de autenticação de usuários
     * @param userDetailsService Serviço de detalhes de usuários
     */
    public SecurityConfiguration(
            UserAuthenticationFilter userAuthenticationFilter,
            UserDetailsService userDetailsService) {
        this.userAuthenticationFilter = userAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        logger.info("SecurityConfiguration inicializada");
    }

    /**
     * Endpoints que não requerem autenticação.
     */
    public static final String[] ENDPOINTS_WITH_AUTHENTICATION_NOT_REQUIRED = {
            "/api/users/login",
            "/api/users",
            "/api/users/validate",
            "/health",
            "/"
    };

    /**
     * Endpoints que requerem autenticação (qualquer usuário autenticado).
     */
    public static final String[] ENDPOINTS_WITH_AUTHENTICATION_REQUIRED = {
            "/api/tribes/**"
    };

    /**
     * Endpoints que requerem papel de usuário comum (ROLE_USER).
     */
    public static final String[] ENDPOINTS_CUSTOMER = {
            "/api/users/test/customer"
    };

    /**
     * Endpoints que requerem papel de administrador (ROLE_ADMIN).
     */
    public static final String[] ENDPOINTS_ADMIN = {
            "/api/users/test/administrator",
            "/api/users/admin/list"
    };

    /**
     * Configura a cadeia de filtros de segurança.
     *
     * @param httpSecurity Configuração de segurança HTTP
     * @return Cadeia de filtros de segurança configurada
     * @throws Exception Se ocorrer um erro durante a configuração
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        logger.info("Configurando SecurityFilterChain");

        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(ENDPOINTS_WITH_AUTHENTICATION_NOT_REQUIRED).permitAll()
                        .requestMatchers(ENDPOINTS_WITH_AUTHENTICATION_REQUIRED).authenticated()
                        .requestMatchers(ENDPOINTS_ADMIN).hasAuthority("ROLE_ADMIN")
                        .requestMatchers(ENDPOINTS_CUSTOMER).hasAuthority("ROLE_USER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.warn("Acesso negado: {}", accessDeniedException.getMessage());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("Você não tem autorização para acessar este recurso.");
                        })
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(userAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("SecurityFilterChain configurado com sucesso");
        return httpSecurity.build();
    }

    /**
     * Configura o provedor de autenticação DAO.
     *
     * @return Provedor de autenticação configurado
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        logger.debug("Configurando DaoAuthenticationProvider");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configura a fonte de configuração CORS.
     * Permite requisições de origens específicas com métodos e cabeçalhos permitidos.
     *
     * @return Fonte de configuração CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.debug("Configurando CorsConfigurationSource");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200", 
                "https://ping-social-front-ym8d.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configura o gerenciador de autenticação.
     *
     * @param authenticationConfiguration Configuração de autenticação
     * @return Gerenciador de autenticação
     * @throws Exception Se ocorrer um erro durante a configuração
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.debug("Configurando AuthenticationManager");
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configura o codificador de senha.
     * Utiliza BCrypt para codificação segura de senhas.
     *
     * @return Codificador de senha
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("Configurando PasswordEncoder (BCrypt)");
        return new BCryptPasswordEncoder();
    }
}
