package com.example.routing_service.repository;

import com.example.routing_service.model.DangerZone;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DangerZoneRepository extends MongoRepository<DangerZone, String> {
    
    @Query("{'location': {$near: {$geometry: {type: 'Point', coordinates: [?0, ?1]}, $maxDistance: ?2}}, 'expiresAt': {$gt: ?3}}")
    List<DangerZone> findNearbyActiveDangerZones(double longitude, double latitude, double maxDistance, LocalDateTime now);
    
    @Query("{'expiresAt': {$lt: ?0}}")
    List<DangerZone> findExpiredDangerZones(LocalDateTime now);
    
    List<DangerZone> findByDangerLevel(DangerZone.DangerLevel dangerLevel);
    
    List<DangerZone> findByTagsContaining(String tag);
    
    @Query("{'reportedByUsers': ?0}")
    List<DangerZone> findByReportedByUsersContaining(String userId);
} 