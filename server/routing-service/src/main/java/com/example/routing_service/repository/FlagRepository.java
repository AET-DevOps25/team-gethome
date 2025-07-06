package com.example.routing_service.repository;

import com.example.routing_service.model.Flag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlagRepository extends MongoRepository<Flag, String> {
    
    @Query("{ $and: [ " +
           "{ 'latitude': { $gte: ?2, $lte: ?3 } }, " +
           "{ 'longitude': { $gte: ?4, $lte: ?5 } } " +
           "] }")
    List<Flag> findFlagsWithinBounds(double lat, double lon, double minLat, double maxLat, double minLon, double maxLon);
    
    List<Flag> findByReportedBy(String userId);
}
