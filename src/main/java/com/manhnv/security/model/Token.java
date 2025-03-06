package com.manhnv.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tokens")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    @Id
    private String id;
    private String token;
    private boolean expired;
    private boolean revoked;
    @DBRef
    private User user;
}
