package com.pingsocial.controller;

import com.pingsocial.dto.CreateUserDto;
import com.pingsocial.dto.LoginUserDto;
import com.pingsocial.dto.RecoveryJwtTokenDto;
import com.pingsocial.dto.ResponseCreateUserDto;
import com.pingsocial.models.User;
import com.pingsocial.repository.UserRepository;
import com.pingsocial.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador responsável por gerenciar as operações relacionadas aos usuários.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Cria um novo usuário.
     *
     * @param createUserDto Dados do usuário a ser criado.
     * @return ResponseEntity contendo os detalhes do usuário criado.
     */
    @PostMapping
    public ResponseEntity<ResponseCreateUserDto> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        ResponseCreateUserDto createdUser = userService.createUser(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Autentica um usuário com base no email e senha fornecidos.
     *
     * @param loginUserDto Dados de login do usuário.
     * @param request      Objeto HttpServletRequest para capturar informações da requisição.
     * @return ResponseEntity contendo o token JWT ou mensagem de erro.
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginUserDto loginUserDto, HttpServletRequest request) {
        ResponseEntity<?> response = userService.authenticateUser(loginUserDto, request);
        return response;
    }

    /**
     * Valida o código de ativação de um usuário.
     *
     * @param email Email do usuário.
     * @param code  Código de validação enviado por email.
     * @return ResponseEntity contendo mensagem de sucesso ou erro.
     */
    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> validateUser(@RequestParam String email, @RequestParam String code) {
        return userService.validateUser(email, code);
    }

    /**
     * Testa a autenticação de um administrador.
     *
     * @return ResponseEntity contendo mensagem de sucesso.
     */
    @GetMapping("/test/administrator")
    public ResponseEntity<String> getAuthenticationTest() {
        return new ResponseEntity<>("Autenticado com sucesso", HttpStatus.OK);
    }

    /**
     * Testa a autenticação de um cliente.
     *
     * @return ResponseEntity contendo mensagem de sucesso.
     */
    @GetMapping("/test/customer")
    public ResponseEntity<String> getCustomerAuthenticationTest() {
        return new ResponseEntity<>("Cliente autenticado com sucesso", HttpStatus.OK);
    }

    /**
     * Retorna a lista de todos os usuários cadastrados.
     *
     * @return ResponseEntity contendo a lista de usuários.
     */
    @GetMapping("/admin/list")
    public ResponseEntity<List<User>> getUsers() {
        return userService.getAllUsers();
    }
}