package com.example.gethome.message.repository;

import com.example.gethome.message.model.EmergencyNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyNotificationRepository extends MongoRepository<EmergencyNotification, String> {
    
    List<EmergencyNotification> findByUserId(String userId);
    
    List<EmergencyNotification> findByUserIdAndStatus(String userId, EmergencyNotification.NotificationStatus status);
    
    List<EmergencyNotification> findByStatus(EmergencyNotification.NotificationStatus status);
    
    @Query("{'expiresAt': {$lt: ?0}}")
    List<EmergencyNotification> findExpiredNotifications(LocalDateTime now);
    
    @Query("{'triggeredAt': {$gte: ?0, $lte: ?1}}")
    List<EmergencyNotification> findByTriggeredAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<EmergencyNotification> findByEmergencyType(String emergencyType);
    
    @Query("{'contactNotifications.contactId': ?0}")
    List<EmergencyNotification> findByContactId(String contactId);
    
    @Query("{'userId': ?0, 'triggeredAt': {$gte: ?1}}")
    List<EmergencyNotification> findByUserIdAndTriggeredAtAfter(String userId, LocalDateTime after);
    
    List<EmergencyNotification> findByStatusIn(List<EmergencyNotification.NotificationStatus> statuses);
} 