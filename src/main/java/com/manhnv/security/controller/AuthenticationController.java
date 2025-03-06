package com.manhnv.security.controller;

import com.manhnv.security.dto.*;
import com.manhnv.security.service.AuthenticationService;
import com.manhnv.security.utils.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    @Value("${application.security.jwt.tokenHeader}")
    private String tokenHeader;

    private final AuthenticationService service;

    @PostMapping("/register")
    public CommonResult<UserResponse> register(
            @Validated @RequestBody RegisterRequest request
    ) {
        return CommonResult.success(service.register(request));
    }
    @PostMapping("/login")
    public CommonResult<LoginResponse> login(
            @Validated @RequestBody LoginRequest request
    ) {
        return CommonResult.success(service.login(request));
    }

    @PostMapping("/refresh-token")
    public CommonResult<?> refreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader(tokenHeader);
        String newAccessToken = service.refreshToken(refreshToken);
        if (newAccessToken == null) {
            return CommonResult.failed("token invalid");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", newAccessToken);
        return CommonResult.success(tokenMap);
    }

    @PostMapping("/logout")
    public CommonResult<?> logout(@RequestParam String refreshToken) {
        service.logout(refreshToken);
        return CommonResult.success("Logout successful");
    }
}
