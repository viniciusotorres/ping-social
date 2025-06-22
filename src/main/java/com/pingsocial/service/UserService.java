package com.pingsocial.service;

import com.pingsocial.config.JwtTokenService;
import com.pingsocial.config.SecurityConfiguration;
import com.pingsocial.config.UserDetailsImpl;
import com.pingsocial.dto.*;
import com.pingsocial.models.Role;
import com.pingsocial.models.RoleName;
import com.pingsocial.models.User;
import com.pingsocial.repository.RoleRepository;
import com.pingsocial.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailService emailService;

    public ResponseEntity<?> authenticateUser(LoginUserDto loginUserDto, HttpServletRequest request) {
        try {
            Authentication authentication = authenticate(loginUserDto);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

            if (!user.isAtivo()) {
                if (user.getValidationCode() == null) {
                    String validationCode = generateValidationCode();
                    user.setValidationCode(validationCode);

                    try {
                        String emailBody = "<html>" +
                                "<body style='font-family: Arial, sans-serif; background-color: #f9f9f9; margin: 0; padding: 0;'>" +
                                "<div style='max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); overflow: hidden;'>" +
                                "<div style='background-color: #4CAF50; color: #ffffff; padding: 20px; text-align: center;'>" +
                                "<h1 style='margin: 0;'>The Tribe</h1>" +
                                "</div>" +
                                "<div style='padding: 20px;'>" +
                                "<p style='font-size: 16px; color: #333;'>Olá,</p>" +
                                "<p style='font-size: 16px; color: #333;'>Obrigado por se registrar na The Tribe! Para ativar sua conta, use o código abaixo:</p>" +
                                "<div style='text-align: center; margin: 20px 0;'>" +
                                "<h3 style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; display: inline-block; color: #333;'>" + validationCode + "</h3>" +
                                "</div>" +
                                "<p style='font-size: 14px; color: #666;'>Se você não solicitou este email, ignore-o.</p>" +
                                "</div>" +
                                "<div style='background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; color: #999;'>" +
                                "<p>© 2023 The Tribe. Todos os direitos reservados.</p>" +
                                "</div>" +
                                "</div>" +
                                "</body>" +
                                "</html>";

                        emailService.sendEmail(
                                user.getEmail(),
                                "Ativação de Conta!",
                                emailBody
                        );
                    } catch (Exception e) {
                        return ResponseEntity.ok(new ResponseIsNotActive(
                                "Erro ao enviar email de ativação. Tente novamente mais tarde.",
                                user.getEmail()
                        ));
                    }
                    userRepository.save(user);
                }
                return ResponseEntity.ok(new ResponseIsNotActive(
                        "Usuário não está ativo. Verifique seu email para ativar a conta.",
                        user.getEmail()
                ));
            }

            updateLastLogin(userDetails.getUsername());

            String clientIp = request.getRemoteAddr();
            String token = jwtTokenService.generateToken(userDetails, clientIp);

            return ResponseEntity.ok(new RecoveryJwtTokenDto(token));
        } catch (Exception e) {
            if (e.getMessage().contains("Bad credentials")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseLoginFailedDto("Senha incorreta.", loginUserDto.email()));
            } else if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseLoginFailedDto("Email não encontrado.", loginUserDto.email()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseLoginFailedDto("Erro ao autenticar usuário: " + e.getMessage(), loginUserDto.email()));
            }
        }}

    public ResponseEntity<String> validateUser(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (user.getValidationCode() != null && user.getValidationCode().equals(code)) {
            user.setAtivo(true);
            user.setValidationCode(null);
            userRepository.save(user);

            try {
                String emailBody = "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #f4f4f4; padding: 20px; border-radius: 10px;'>" +
                        "<h2 style='color: #333;'>Bem-vindo à The Tribe!</h2>" +
                        "<p>Olá,</p>" +
                        "<p>Estamos felizes em informar que sua conta foi ativada com sucesso e está pronta para uso.</p>" +
                        "<p>Agora você faz parte da nossa tribo, onde conectamos pessoas e ideias.</p>" +
                        "<p style='margin-top: 20px;'>Se precisar de ajuda ou tiver dúvidas, entre em contato conosco.</p>" +
                        "<p style='margin-top: 20px;'>Atenciosamente,</p>" +
                        "<p><strong>Equipe The Tribe</strong></p>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

                emailService.sendEmail(
                        user.getEmail(),
                        "Bem-vindo à The Tribe!",
                        emailBody
                );
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar email de confirmação: " + e.getMessage());
            }

            return ResponseEntity.ok("Usuário ativado com sucesso.");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código de validação inválido.");
    }

    private Authentication authenticate(LoginUserDto loginUserDto) {
        try {
            System.out.println("Tentando autenticar usuário: " + loginUserDto.email());
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginUserDto.email(), loginUserDto.password());
            return authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao autenticar usuário: " + e.getMessage());
        }
    }

    private void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setUltimoLogin(java.time.LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public ResponseCreateUserDto createUser(CreateUserDto createUserDto) {
        try {
            Role role = roleRepository.findByName(RoleName.valueOf(createUserDto.role().toUpperCase()))
                    .orElseThrow(() -> new RuntimeException("O papel especificado não existe: " + createUserDto.role()));

            User newUser = new User(
                    null,
                    createUserDto.email(),
                    securityConfiguration.passwordEncoder().encode(createUserDto.password()),
                    List.of(role),
                    null,
                    null,
                    null,
                    java.time.LocalDateTime.now()
            );

            userRepository.save(newUser);

            return new ResponseCreateUserDto("Usuário criado com sucesso.", newUser.getId(), createUserDto.email(), java.time.LocalDateTime.now().toString());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Erro ao criar usuário: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao criar usuário: " + e.getMessage());
        }
    }

    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    private String generateValidationCode() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }


}
