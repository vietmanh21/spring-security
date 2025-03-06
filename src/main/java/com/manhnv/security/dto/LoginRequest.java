package com.manhnv.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
public class LoginRequest {
    @Email
    private String email;
    @NotEmpty
    private String password;
}
