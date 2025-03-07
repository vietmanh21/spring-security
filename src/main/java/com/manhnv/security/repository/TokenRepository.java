package com.manhnv.security.repository;

import com.manhnv.security.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByIdAndExpiresAtAfter(String id, Instant expiresAtAfter);
}
