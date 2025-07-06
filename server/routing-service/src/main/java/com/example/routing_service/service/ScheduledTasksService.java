package com.example.routing_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksService {

    private final DangerZoneService dangerZoneService;

    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    public void cleanupExpiredDangerZones() {
        log.info("Starting scheduled cleanup of expired danger zones");
        try {
            dangerZoneService.cleanupExpiredZones();
            log.info("Scheduled cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled cleanup: {}", e.getMessage(), e);
        }
    }
} 