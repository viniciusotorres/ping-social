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
                            "<body>" +
                            "<p>Olá,</p>" +
                            "<p>Obrigado por se registrar no PingSocial. Para ativar sua conta, use o código abaixo:</p>" +
                            "<h3>" + validationCode + "</h3>" +
                            "<p>Se você não solicitou este email, ignore-o.</p>" +
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
    }

    public ResponseEntity<String> validateUser(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (user.getValidationCode() != null && user.getValidationCode().equals(code)) {
            user.setAtivo(true);
            user.setValidationCode(null);
            userRepository.save(user);

            try {
                String emailBody = "<html>" +
                        "<body>" +
                        "<p>Olá,</p>" +
                        "<p>Sua conta foi ativada e está pronta para uso.</p>" +
                        "</body>" +
                        "</html>";

                emailService.sendEmail(
                        user.getEmail(),
                        "Conta ativada com sucesso!",
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

            return new ResponseCreateUserDto("Usuário criado com sucesso.", newUser.getId(), createUserDto.email(),java.time.LocalDateTime.now().toString());
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
