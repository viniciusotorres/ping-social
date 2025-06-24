package com.pingsocial.service;

import com.pingsocial.config.JwtTokenService;
import com.pingsocial.config.SecurityConfiguration;
import com.pingsocial.config.UserDetailsImpl;
import com.pingsocial.dto.*;
import com.pingsocial.exception.AuthenticationException;
import com.pingsocial.exception.EmailSendingException;
import com.pingsocial.exception.InvalidValidationCodeException;
import com.pingsocial.exception.UserNotFoundException;
import com.pingsocial.models.Role;
import com.pingsocial.models.RoleName;
import com.pingsocial.models.User;
import com.pingsocial.repository.RoleRepository;
import com.pingsocial.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço responsável por operações relacionadas a usuários.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final SecurityConfiguration securityConfiguration;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    /**
     * Construtor com injeção de dependências.
     */
    public UserService(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            UserRepository userRepository,
            SecurityConfiguration securityConfiguration,
            RoleRepository roleRepository,
            EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.securityConfiguration = securityConfiguration;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
    }

    /**
     * Autentica um usuário e retorna um token JWT se a autenticação for bem-sucedida.
     * Se o usuário não estiver ativo, envia um email de ativação.
     *
     * @param loginUserDto DTO contendo as credenciais do usuário
     * @param request      Requisição HTTP para obter o IP do cliente
     * @return ResponseEntity contendo o token JWT ou informações sobre o erro
     */
    @Transactional
    public ResponseEntity<?> authenticateUser(LoginUserDto loginUserDto, HttpServletRequest request) {
        logger.info("Tentando autenticar usuário: {}", loginUserDto.email());

        try {
            // Autenticar usuário
            Authentication authentication = authenticate(loginUserDto);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Buscar usuário no banco de dados
            User user = findUserByEmail(userDetails.getUsername());

            // Verificar se o usuário está ativo
            if (!user.isAtivo()) {
                logger.info("Usuário {} não está ativo", user.getEmail());
                ResponseIsNotActive response = handleInactiveUser(user);
                return ResponseEntity.ok(response);
            }

            // Atualizar último login e gerar token
            updateLastLogin(user);
            String clientIp = request.getRemoteAddr();
            String token = jwtTokenService.generateToken(userDetails, clientIp);

            logger.info("Usuário {} autenticado com sucesso", user.getEmail());
            return ResponseEntity.ok(new ResponseLoginSuccessDto(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getLatitude(),
                    user.getLongitude(),
                    user.isAtivo(),
                    user.getNickname()
            ));
        } catch (BadCredentialsException e) {
            logger.error("Credenciais inválidas para o usuário: {}", loginUserDto.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseLoginFailedDto("Senha incorreta.", loginUserDto.email()));
        } catch (UserNotFoundException e) {
            logger.error("Usuário não encontrado: {}", loginUserDto.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseLoginFailedDto("Email não encontrado.", loginUserDto.email()));
        } catch (Exception e) {
            logger.error("Erro ao autenticar usuário: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseLoginFailedDto("Erro ao autenticar usuário: " + e.getMessage(), loginUserDto.email()));
        }
    }

    /**
     * Valida um código de ativação para um usuário.
     *
     * @param email Email do usuário
     * @param code  Código de validação
     * @return ResponseEntity com mensagem de sucesso ou erro
     */
    @Transactional
    public ResponseEntity<String> validateUser(String email, String code) {
        logger.info("Validando código para o usuário: {}", email);

        try {
            User user = findUserByEmail(email);

            if (user.getValidationCode() == null || !user.getValidationCode().equals(code)) {
                logger.warn("Código de validação inválido para o usuário: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código de validação inválido.");
            }

            // Ativar usuário
            user.setAtivo(true);
            user.setValidationCode(null);
            userRepository.save(user);

            // Enviar email de boas-vindas
            try {
                sendWelcomeEmail(user);
                logger.info("Usuário {} ativado com sucesso", email);
                return ResponseEntity.ok("Usuário ativado com sucesso.");
            } catch (Exception e) {
                logger.error("Erro ao enviar email de boas-vindas: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao enviar email de confirmação: " + e.getMessage());
            }
        } catch (UserNotFoundException e) {
            logger.error("Usuário não encontrado: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao validar usuário: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao validar usuário: " + e.getMessage());
        }
    }

    /**
     * Cria um novo usuário.
     *
     * @param createUserDto DTO contendo os dados do novo usuário
     * @return DTO com informações sobre o usuário criado
     * @throws IllegalArgumentException se o papel especificado não existir
     * @throws RuntimeException         se ocorrer um erro ao criar o usuário
     */
    @Transactional
    public ResponseCreateUserDto createUser(CreateUserDto createUserDto) {
        logger.info("Criando novo usuário com email: {}", createUserDto.email());

        try {
            // Validar e obter o papel do usuário
            Role role = findRole(createUserDto.role());

            // Criar novo usuário
            User newUser = new User(
                    null,
                    createUserDto.email(),
                    securityConfiguration.passwordEncoder().encode(createUserDto.password()),
                    List.of(role),
                    null,
                    null,
                    null,
                    LocalDateTime.now(),
                    createUserDto.nickname()
            );

            userRepository.save(newUser);

            logger.info("Usuário criado com sucesso: {}", newUser.getEmail());
            return new ResponseCreateUserDto(
                    "Usuário criado com sucesso.",
                    newUser.getId(),
                    createUserDto.email(),
                    LocalDateTime.now().toString()
            );
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao criar usuário - papel inválido: {}", e.getMessage());
            throw new RuntimeException("Erro ao criar usuário: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Erro inesperado ao criar usuário: " + e.getMessage());
        }
    }

    /**
     * Obtém todos os usuários.
     *
     * @return ResponseEntity contendo a lista de todos os usuários
     */
    @Transactional(readOnly = true)
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Obtendo todos os usuários");
        try {
            String emailAutenticado = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            List<User> users = userRepository.findAll().stream()
                    .filter(user -> !user.getEmail().equalsIgnoreCase(emailAutenticado))
                    .toList();

            logger.info("Encontrados {} usuários (excluindo o próprio)", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Erro ao obter usuários: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Autentica um usuário com suas credenciais.
     *
     * @param loginUserDto DTO contendo as credenciais do usuário
     * @return Objeto Authentication
     * @throws AuthenticationException se ocorrer um erro na autenticação
     */
    private Authentication authenticate(LoginUserDto loginUserDto) {
        try {
            logger.debug("Autenticando usuário: {}", loginUserDto.email());
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginUserDto.email(), loginUserDto.password());
            return authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            logger.error("Credenciais inválidas para o usuário: {}", loginUserDto.email());
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao autenticar usuário: {}", e.getMessage(), e);
            throw new AuthenticationException("Erro ao autenticar usuário: " + e.getMessage(), loginUserDto.email());
        }
    }

    /**
     * Atualiza a data do último login do usuário.
     *
     * @param user Usuário a ser atualizado
     */
    private void updateLastLogin(User user) {
        logger.debug("Atualizando último login para o usuário: {}", user.getEmail());
        user.setUltimoLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Gera um código de validação aleatório de 4 dígitos.
     *
     * @return Código de validação
     */
    private String generateValidationCode() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }

    /**
     * Busca um usuário pelo email.
     *
     * @param email Email do usuário
     * @return Usuário encontrado
     * @throws UserNotFoundException se o usuário não for encontrado
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado com email: {}", email);
                    return new UserNotFoundException("Usuário não encontrado com email: " + email);
                });
    }

    /**
     * Busca um papel pelo enum RoleName.
     *
     * @param roleName Enum do papel
     * @return Papel encontrado
     * @throws IllegalArgumentException se o papel não existir
     */
    private Role findRole(RoleName roleName) {
        if (roleName == null) {
            logger.error("Papel não pode ser nulo");
            throw new IllegalArgumentException("O papel não pode ser nulo");
        }

        return roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    logger.error("Papel não encontrado: {}", roleName);
                    return new IllegalArgumentException("O papel especificado não existe: " + roleName);
                });
    }

    /**
     * Trata o caso de um usuário inativo.
     * Se o usuário não tiver um código de validação, gera um novo e envia por email.
     *
     * @param user Usuário inativo
     * @return Objeto de resposta indicando que o usuário não está ativo
     */
    private ResponseIsNotActive handleInactiveUser(User user) {
        if (user.getValidationCode() == null) {
            String validationCode = generateValidationCode();
            user.setValidationCode(validationCode);

            try {
                sendActivationEmail(user, validationCode);
                userRepository.save(user);
                logger.info("Email de ativação enviado para: {}", user.getEmail());
            } catch (Exception e) {
                logger.error("Erro ao enviar email de ativação: {}", e.getMessage(), e);
                return new ResponseIsNotActive(
                        "Erro ao enviar email de ativação. Tente novamente mais tarde.",
                        user.getEmail()
                );
            }
        }

        return new ResponseIsNotActive(
                "Usuário não está ativo. Verifique seu email para ativar a conta.",
                user.getEmail()
        );
    }

    /**
     * Envia um email de ativação para o usuário.
     *
     * @param user           Usuário para quem o email será enviado
     * @param validationCode Código de validação
     * @throws EmailSendingException se ocorrer um erro ao enviar o email
     */
    private void sendActivationEmail(User user, String validationCode) {
        String emailBody = generateActivationEmailTemplate(validationCode);

        try {
            emailService.sendEmail(
                    user.getEmail(),
                    "Ativação de Conta!",
                    emailBody
            );
        } catch (Exception e) {
            logger.error("Erro ao enviar email de ativação: {}", e.getMessage(), e);
            throw new EmailSendingException("Erro ao enviar email de ativação: " + e.getMessage());
        }
    }

    /**
     * Envia um email de boas-vindas para o usuário.
     *
     * @param user Usuário para quem o email será enviado
     * @throws EmailSendingException se ocorrer um erro ao enviar o email
     */
    private void sendWelcomeEmail(User user) {
        String emailBody = generateWelcomeEmailTemplate();

        try {
            emailService.sendEmail(
                    user.getEmail(),
                    "Bem-vindo à The Tribe!",
                    emailBody
            );
        } catch (Exception e) {
            logger.error("Erro ao enviar email de boas-vindas: {}", e.getMessage(), e);
            throw new EmailSendingException("Erro ao enviar email de boas-vindas: " + e.getMessage());
        }
    }

    /**
     * Gera o template HTML para o email de ativação.
     *
     * @param validationCode Código de validação
     * @return Template HTML do email
     */
    private String generateActivationEmailTemplate(String validationCode) {
        return "<html>" +
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
    }

    /**
     * Gera o template HTML para o email de boas-vindas.
     *
     * @return Template HTML do email
     */
    private String generateWelcomeEmailTemplate() {
        return "<html>" +
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
    }

    /**
     * Busca o ID do usuário pelo email.
     *
     * @param email Email do usuário
     * @return ID do usuário
     * @throws UserNotFoundException se o usuário não for encontrado
     */
    public Long findIdByEmail(String email) {
        logger.info("Buscando ID do usuário pelo email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com email: " + email));
        return user.getId();
    }
}
