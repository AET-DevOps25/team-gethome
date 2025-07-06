package com.example.gethome.message.repository;

import com.example.gethome.message.model.MessageLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageLogRepository extends MongoRepository<MessageLog, String> {
    
    List<MessageLog> findByUserId(String userId);
    
    List<MessageLog> findByNotificationId(String notificationId);
    
    List<MessageLog> findByStatus(MessageLog.MessageStatus status);
    
    List<MessageLog> findByMessageType(MessageLog.MessageType messageType);
    
    @Query("{'sentAt': {$gte: ?0, $lte: ?1}}")
    List<MessageLog> findBySentAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{'nextRetryAt': {$lte: ?0}}")
    List<MessageLog> findMessagesReadyForRetry(LocalDateTime now);
    
    List<MessageLog> findByContactEmail(String contactEmail);
    
    List<MessageLog> findByContactPhone(String contactPhone);
} 