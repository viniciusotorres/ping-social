package com.pingsocial.service;

import com.pingsocial.config.JwtTokenService;
import com.pingsocial.config.SecurityConfiguration;
import com.pingsocial.config.UserDetailsImpl;
import com.pingsocial.dto.CreateUserDto;
import com.pingsocial.dto.LoginUserDto;
import com.pingsocial.dto.RecoveryJwtTokenDto;
import com.pingsocial.models.Role;
import com.pingsocial.models.User;
import com.pingsocial.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    private GeoLocationService geoLocationService;

    public RecoveryJwtTokenDto authenticateUser(LoginUserDto loginUserDto, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginUserDto.email(), loginUserDto.password());

        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);


        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String clientIp = request.getRemoteAddr();

        Optional<User> user = userRepository.findByEmail(userDetails.getUsername());

        if (user != null) {
            user.ifPresent(u -> {
                u.setUltimoLogin(java.time.LocalDateTime.now());
            });
            userRepository.save(user.get());
        }

        return new RecoveryJwtTokenDto(jwtTokenService.generateToken(userDetails, clientIp));
    }


    public void createUser(CreateUserDto createUserDto, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        Role role = new Role();
        role.setName(createUserDto.role());

        Double latitude = null;
        Double longitude = null;

        try {
            GeoLocationService.GeoLocationResponse locationResponse = geoLocationService.getLocation(clientIp);
            if (locationResponse != null && "success".equals(locationResponse.getStatus())) {
                latitude = locationResponse.getLat();
                longitude = locationResponse.getLon();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        User newUser = new User(
                null,
                createUserDto.email(),
                securityConfiguration.passwordEncoder().encode(createUserDto.password()),
                List.of(role),
                latitude, // Latitude
                longitude, // Longitude
                null,
                java.time.LocalDateTime.now()
        );

        userRepository.save(newUser);
    }
}
