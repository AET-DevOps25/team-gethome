package com.usermanagement_service.repository;

import com.usermanagement_service.model.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    Optional<UserProfile> findByUserId(String userId);
    List<UserProfile> findByAliasContainingIgnoreCase(String alias);
} 