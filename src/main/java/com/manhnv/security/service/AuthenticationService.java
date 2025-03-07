package com.manhnv.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manhnv.security.component.JwtService;
import com.manhnv.security.dto.*;
import com.manhnv.security.exception.CustomException;
import com.manhnv.security.model.RefreshToken;
import com.manhnv.security.repository.RoleRepository;
import com.manhnv.security.repository.TokenRepository;
import com.manhnv.security.repository.UserRepository;
import com.manhnv.security.model.Role;
import com.manhnv.security.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final RedisService redisService;

    private static final String ROLE_USER = "USER";

    public UserResponse register(RegisterRequest request) {
        UserResponse userResponse = new UserResponse();
        if (repository.existsByEmail(request.getEmail())) {
            throw new CustomException("Username is already in use");
        }
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null) {
            Role userRole = roleRepository.findByName(ROLE_USER).orElseThrow(() -> new CustomException("Role is not found"));
            roles.add(userRole);
        } else {
            request.getRoles().forEach(role -> {
                Role userRole = roleRepository.findByName(role).orElseThrow(() -> new CustomException("Role is not found"));
                roles.add(userRole);
            });
        }
        user.setRoles(roles);
        repository.save(user);
        BeanUtils.copyProperties(user, userResponse);
        userResponse.setRoles(roles.stream().map(Role::getName).collect(Collectors.toSet()));
        return userResponse;

    }

    public LoginResponse login(LoginRequest request) {
        LoginResponse loginResponse = new LoginResponse();
        User userDetails = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Incorrect Email"));

        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new CustomException("Incorrect password");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException ex) {
            log.warn("Authentication exception: {}", ex.getMessage());
            throw new CustomException("Authentication failed: " + ex.getMessage());
        }

        String jwtToken = jwtService.generateToken(userDetails.getUsername());
        String refreshToken = UUID.randomUUID().toString();
        redisService.set(refreshToken, userDetails.getUsername(), refreshExpiration);
        BeanUtils.copyProperties(userDetails, loginResponse);
        loginResponse.setRoles(userDetails.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        loginResponse.setAccessToken(jwtToken);
        loginResponse.setRefreshToken(refreshToken);
        return loginResponse;
    }

    public RefreshTokenResponse refreshToken(String oldToken) {
        String userEmail = (String) redisService.get(oldToken);
        if (userEmail == null) {
            return null;
        }
        redisService.del(oldToken);
        String jwtToken = jwtService.generateToken(userEmail);
        String refreshToken = UUID.randomUUID().toString();
        redisService.set(refreshToken, userEmail, refreshExpiration);
        return new RefreshTokenResponse(jwtToken, refreshToken);
    }

    public void logout(String refreshToken) {
        String userEmail = (String) redisService.get(refreshToken);
        if (userEmail == null) {
            throw new CustomException("Token not found");
        }
        redisService.del(refreshToken);
    }
}
