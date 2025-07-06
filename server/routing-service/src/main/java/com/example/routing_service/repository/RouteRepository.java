package com.example.routing_service.repository;

import com.example.routing_service.model.Route;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RouteRepository extends MongoRepository<Route, String> {
    
    List<Route> findByUserId(String userId);
    
    List<Route> findByUserIdAndStatus(String userId, Route.RouteStatus status);
    
    List<Route> findByExpiresAtBefore(LocalDateTime expiresAt);
    
    List<Route> findByStatus(Route.RouteStatus status);
} 