package com.manhnv.security.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
public class UserResponse {
    private String firstname;
    private String lastname;
    private String email;
    private Set<String> roles;
}
