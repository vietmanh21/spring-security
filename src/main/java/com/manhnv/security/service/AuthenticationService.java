package com.manhnv.security.service;

import com.manhnv.security.component.JwtService;
import com.manhnv.security.dto.LoginRequest;
import com.manhnv.security.dto.LoginResponse;
import com.manhnv.security.dto.RegisterRequest;
import com.manhnv.security.dto.UserResponse;
import com.manhnv.security.exception.CustomException;
import com.manhnv.security.model.Token;
import com.manhnv.security.repository.RoleRepository;
import com.manhnv.security.repository.TokenRepository;
import com.manhnv.security.repository.UserRepository;
import com.manhnv.security.model.Role;
import com.manhnv.security.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {


    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;

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

        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        revokeAllUserTokens(userDetails);
        saveUserToken(userDetails, refreshToken);
        BeanUtils.copyProperties(userDetails, loginResponse);
        loginResponse.setRoles(userDetails.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        loginResponse.setAccessToken(jwtToken);
        loginResponse.setRefreshToken(refreshToken);
        return loginResponse;
    }

    public String refreshToken(String oldToken) {
        String refreshToken = oldToken.substring(7);
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user = this.repository.findByEmail(userEmail)
                    .orElseThrow(() -> new CustomException("User not found in refreshToken"));
            if (jwtService.isTokenValid(refreshToken, user)) {
                return jwtService.generateToken(user);
            } else {
                revokeAllUserTokens(user);
            }
        }
        return null;
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void logout(String refreshToken) {
        Token token = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException("Token not found"));
        token.setRevoked(true);
        tokenRepository.save(token);
    }
}
