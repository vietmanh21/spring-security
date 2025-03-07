package com.manhnv.security.controller;

import com.manhnv.security.dto.*;
import com.manhnv.security.service.AuthenticationService;
import com.manhnv.security.utils.CommonResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    private final AuthenticationService service;

    @PostMapping("/register")
    public CommonResult<UserResponse> register(
            @Validated @RequestBody RegisterRequest request
    ) {
        return CommonResult.success(service.register(request));
    }
    @PostMapping("/login")
    public CommonResult<LoginResponse> login(
            @Validated @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResponse loginResponse = service.login(request);
        // Lưu refresh token vào HttpOnly cookie
        Cookie refreshCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/auth");
        refreshCookie.setMaxAge((int) (refreshExpiration / 1000));

        response.addCookie(refreshCookie);
        return CommonResult.success(loginResponse);
    }

    @PostMapping("/refresh-token")
    public CommonResult<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            return CommonResult.unauthorized("No refresh token found.");
        }

        RefreshTokenResponse newToken = service.refreshToken(refreshToken);
        if (newToken == null) {
            return CommonResult.failed("token invalid");
        }
        return CommonResult.success(newToken);
    }

    @PostMapping("/logout")
    public CommonResult<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            return CommonResult.unauthorized("No refresh token found.");
        }
        service.logout(refreshToken);
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        return CommonResult.success("Logout successful");
    }
}
