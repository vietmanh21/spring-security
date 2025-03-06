package com.manhnv.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter
@Setter
public class RegisterRequest {
    private String firstname;
    private String lastname;
    @Email
    private String email;
    @NotEmpty
    private String password;
    private Set<String> roles;
}
