package com.manhnv.security.controller;

import com.manhnv.security.utils.CommonResult;
import com.manhnv.security.dto.UserResponse;
import com.manhnv.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("user/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CommonResult<UserResponse> getUserProfile(final Authentication authentication) {
        UserResponse result = userService.profile(authentication.getName());
        return CommonResult.success(result);
    }

    @GetMapping("admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResult<List<UserResponse>> listUser() {
        List<UserResponse> result = userService.listUser();
        return CommonResult.success(result);
    }
}
