package com.pingsocial.config;

import com.pingsocial.models.User;
import com.pingsocial.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Filtro de autenticação baseado em JWT.
 * Intercepta todas as requisições HTTP e verifica se o token JWT é válido.
 * Se o token for válido, configura a autenticação no contexto de segurança.
 */
@Component
public class UserAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthenticationFilter.class);
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (checkIfEndpointIsNotPublic(request)) {
            String token = recoveryToken(request);
            if (token != null) {
                try {
                    String subject = jwtTokenService.getSubjectFromToken(token);
                    User user = userRepository.findByEmail(subject).orElse(null);

                    if (user != null) {
                        UserDetailsImpl userDetails = new UserDetailsImpl(user);
                        Authentication authentication =
                                new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        if (!hasRequiredRole(user, request.getRequestURI())) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("Acesso negado: permissão insuficiente.");
                            return;
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Usuário não encontrado.");
                        return;
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token inválido.");
                    return;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("O token está ausente.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoveryToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }

        return null;
    }


    private boolean hasRequiredRole(User user, String requestURI) {
        if (requestURI.startsWith("/admin")) {
            return user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        }
        return true;
    }

    private boolean checkIfEndpointIsNotPublic(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        boolean isPublic = Arrays.asList(SecurityConfiguration.ENDPOINTS_WITH_AUTHENTICATION_NOT_REQUIRED).contains(requestURI);
        boolean isSwagger = Arrays.stream(SecurityConfiguration.SWAGGER_WHITELIST)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
        return !(isPublic || isSwagger);
    }
}
