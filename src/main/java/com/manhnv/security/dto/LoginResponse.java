package com.manhnv.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

@Data
public class LoginResponse {
    private String firstname;
    private String lastname;
    private String email;
    private Set<String> roles;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
}
