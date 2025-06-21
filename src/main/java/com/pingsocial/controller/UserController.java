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

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ResponseCreateUserDto> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        ResponseCreateUserDto createdUser = userService.createUser(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginUserDto loginUserDto, HttpServletRequest request) {
        ResponseEntity<?> response = userService.authenticateUser(loginUserDto, request);
        return response;
    }

    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> validateUser(@RequestParam String email, @RequestParam String code) {
        return userService.validateUser(email, code);
    }

    @GetMapping("/test/administrator")
    public ResponseEntity<String> getAuthenticationTest() {
        return new ResponseEntity<>("Autenticado com sucesso", HttpStatus.OK);
    }

    @GetMapping("/test/customer")
    public ResponseEntity<String> getCustomerAuthenticationTest() {
        return new ResponseEntity<>("Cliente autenticado com sucesso", HttpStatus.OK);
    }

    @GetMapping("/admin/list")
    public ResponseEntity<List<User>> getUsers() {
        return userService.getAllUsers();
    }
}
