package com.example.gethome.message.service;

import com.example.gethome.message.model.MessageLog;
import com.example.gethome.message.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;

    public List<MessageLog> getUserMessageLogs(String userId) {
        log.info("Getting message logs for user: {}", userId);
        return messageLogRepository.findByUserId(userId);
    }

    public MessageLog getMessageLog(String logId, String userId) {
        log.info("Getting message log {} for user: {}", logId, userId);
        
        MessageLog messageLog = messageLogRepository.findById(logId)
            .orElseThrow(() -> new RuntimeException("Message log not found"));
        
        if (!messageLog.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return messageLog;
    }

    public List<MessageLog> getMessageLogsByStatus(MessageLog.MessageStatus status) {
        log.info("Getting message logs with status: {}", status);
        return messageLogRepository.findByStatus(status);
    }

    public List<MessageLog> getMessageLogsByType(MessageLog.MessageType messageType) {
        log.info("Getting message logs with type: {}", messageType);
        return messageLogRepository.findByMessageType(messageType);
    }

    public List<MessageLog> getMessageLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        log.info("Getting message logs between {} and {}", start, end);
        return messageLogRepository.findBySentAtBetween(start, end);
    }

    public List<MessageLog> getFailedMessages() {
        log.info("Getting failed message logs");
        return messageLogRepository.findByStatus(MessageLog.MessageStatus.FAILED);
    }

    public List<MessageLog> getMessagesReadyForRetry() {
        log.info("Getting messages ready for retry");
        return messageLogRepository.findMessagesReadyForRetry(LocalDateTime.now());
    }

    public void retryFailedMessage(String logId) {
        log.info("Retrying failed message: {}", logId);
        
        MessageLog messageLog = messageLogRepository.findById(logId)
            .orElseThrow(() -> new RuntimeException("Message log not found"));
        
        if (messageLog.getStatus() != MessageLog.MessageStatus.FAILED) {
            throw new RuntimeException("Message is not in failed status");
        }
        
        messageLog.setStatus(MessageLog.MessageStatus.RETRY);
        messageLog.setRetryCount(messageLog.getRetryCount() + 1);
        messageLog.setNextRetryAt(LocalDateTime.now().plusMinutes(5)); // Retry after 5 minutes
        
        messageLogRepository.save(messageLog);
        log.info("Message {} marked for retry", logId);
    }

    public void updateMessageStatus(String logId, MessageLog.MessageStatus status, String deliveryId) {
        log.info("Updating message log {} status to: {}", logId, status);
        
        MessageLog messageLog = messageLogRepository.findById(logId)
            .orElseThrow(() -> new RuntimeException("Message log not found"));
        
        messageLog.setStatus(status);
        messageLog.setDeliveryId(deliveryId);
        
        if (status == MessageLog.MessageStatus.DELIVERED) {
            messageLog.setDeliveredAt(LocalDateTime.now());
        }
        
        messageLogRepository.save(messageLog);
    }

    public void markMessageAsFailed(String logId, String errorMessage) {
        log.info("Marking message log {} as failed: {}", logId, errorMessage);
        
        MessageLog messageLog = messageLogRepository.findById(logId)
            .orElseThrow(() -> new RuntimeException("Message log not found"));
        
        messageLog.setStatus(MessageLog.MessageStatus.FAILED);
        messageLog.setErrorMessage(errorMessage);
        
        messageLogRepository.save(messageLog);
    }

    public List<MessageLog> getMessageLogsByContactEmail(String contactEmail) {
        log.info("Getting message logs for contact email: {}", contactEmail);
        return messageLogRepository.findByContactEmail(contactEmail);
    }

    public List<MessageLog> getMessageLogsByContactPhone(String contactPhone) {
        log.info("Getting message logs for contact phone: {}", contactPhone);
        return messageLogRepository.findByContactPhone(contactPhone);
    }

    public void cleanupOldLogs(LocalDateTime cutoffDate) {
        log.info("Cleaning up message logs older than: {}", cutoffDate);
        
        List<MessageLog> oldLogs = messageLogRepository.findBySentAtBetween(
            LocalDateTime.MIN, cutoffDate);
        
        if (!oldLogs.isEmpty()) {
            messageLogRepository.deleteAll(oldLogs);
            log.info("Deleted {} old message logs", oldLogs.size());
        }
    }
} 