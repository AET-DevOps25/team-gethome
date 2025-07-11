package com.authservice.repository;

import com.authservice.model.User;
import com.authservice.model.AuthProvider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    
    @Query(value = "{ 'emailVerified': false }", count = true)
    Long countByEmailVerifiedFalse();
    
    @Query(value = "{ 'provider': ?0 }", count = true)
    Long countByProvider(AuthProvider provider);
    
    @Query(value = "{ 'enabled': true }", count = true)
    Long countByEnabledTrue();
}