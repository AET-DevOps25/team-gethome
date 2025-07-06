package com.usermanagement_service.repository;

import com.usermanagement_service.model.AuthUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends MongoRepository<AuthUser, String> {
    Optional<AuthUser> findByEmail(String email);
    List<AuthUser> findByEmailContainingIgnoreCaseOrIdContainingIgnoreCase(String email, String id);
} 