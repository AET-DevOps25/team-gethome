package com.example.routing_service.repository;

import com.example.routing_service.model.Route;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends MongoRepository<Route, String> {
    
    List<Route> findByUserId(String userId);
    
    Optional<Route> findByJourneyId(String journeyId);
    
    List<Route> findByUserIdAndStatus(String userId, Route.RouteStatus status);
}
