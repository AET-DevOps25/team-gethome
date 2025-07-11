package com.example.routing_service.repository;

import com.example.routing_service.model.DangerZone;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DangerZoneRepository extends MongoRepository<DangerZone, String> {
    
    @Query("{ 'location': { $near: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: ?2 } }, 'expiresAt': { $gt: ?3 } }")
    List<DangerZone> findNearbyActiveDangerZones(double longitude, double latitude, double radiusMeters, LocalDateTime currentTime);
    
    @Query("{ 'expiresAt': { $gt: ?0 } }")
    List<DangerZone> findAllActiveDangerZones(LocalDateTime currentTime);
    
    @Query(value = "{ 'expiresAt': { $gt: ?0 } }", count = true)
    Long countActiveDangerZones(LocalDateTime currentTime);
    
    @Query("{ 'reportedBy': ?0 }")
    List<DangerZone> findByReportedBy(String userId);
    
    @Query("{ 'dangerLevel': ?0, 'expiresAt': { $gt: ?1 } }")
    List<DangerZone> findByDangerLevelAndActive(String dangerLevel, LocalDateTime currentTime);
    
    @Query("{ 'tags': { $in: ?0 }, 'expiresAt': { $gt: ?1 } }")
    List<DangerZone> findByTagsInAndActive(List<String> tags, LocalDateTime currentTime);
    
    // Additional methods for DangerZoneService
    @Query("{ 'dangerLevel': ?0 }")
    List<DangerZone> findByDangerLevel(DangerZone.DangerLevel level);
    
    @Query("{ 'tags': { $regex: ?0, $options: 'i' } }")
    List<DangerZone> findByTagsContaining(String tag);
    
    @Query("{ 'reportedByUsers': { $in: [?0] } }")
    List<DangerZone> findByReportedByUsersContaining(String userId);
    
    @Query("{ 'expiresAt': { $lt: ?0 } }")
    List<DangerZone> findExpiredDangerZones(LocalDateTime currentTime);
} 