package com.example.gethome.message.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksService {

    private final MessageLogService messageLogService;
    private final MessageTemplateService messageTemplateService;

    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    public void cleanupOldMessageLogs() {
        log.info("Starting scheduled cleanup of old message logs");
        try {
            // Clean up logs older than 30 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            messageLogService.cleanupOldLogs(cutoffDate);
            log.info("Scheduled cleanup of old message logs completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled cleanup of old message logs: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 */5 * * * ?") // Run every 5 minutes
    public void retryFailedMessages() {
        log.info("Starting scheduled retry of failed messages");
        try {
            // Get messages ready for retry
            var failedMessages = messageLogService.getMessagesReadyForRetry();
            
            for (var messageLog : failedMessages) {
                if (messageLog.getRetryCount() < 3) { // Max 3 retries
                    messageLogService.retryFailedMessage(messageLog.getId());
                    log.info("Retrying failed message: {}", messageLog.getId());
                } else {
                    log.warn("Message {} has exceeded max retry attempts", messageLog.getId());
                }
            }
            
            log.info("Scheduled retry of failed messages completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled retry of failed messages: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM every day
    public void initializeDefaultTemplates() {
        log.info("Starting scheduled initialization of default templates");
        try {
            messageTemplateService.initializeDefaultTemplates();
            log.info("Scheduled initialization of default templates completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled initialization of default templates: {}", e.getMessage(), e);
        }
    }
} 