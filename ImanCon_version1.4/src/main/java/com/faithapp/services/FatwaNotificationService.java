package com.faithapp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FatwaNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FatwaNotificationService.class);
    
    private final ExecutorService executorService;
    private final AtomicBoolean isRunning;
    private final ConcurrentHashMap<String, NotificationListener> listeners;
    
    public FatwaNotificationService() {
        this.executorService = Executors.newCachedThreadPool();
        this.isRunning = new AtomicBoolean(false);
        this.listeners = new ConcurrentHashMap<>();
    }
    
    public void startService() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Fatwa notification service started");
        }
    }
    
    public void stopService() {
        if (isRunning.compareAndSet(true, false)) {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
                listeners.clear();
                logger.info("Fatwa notification service stopped");
            } catch (Exception e) {
                logger.error("Error stopping notification service", e);
            }
        }
    }
    
    public void registerScholarListener(String scholarId, NotificationListener listener) {
        listeners.put("SCHOLAR_" + scholarId, listener);
        logger.info("Registered notification listener for scholar: {}", scholarId);
    }
    
    public void registerUserListener(String userId, NotificationListener listener) {
        listeners.put("USER_" + userId, listener);
        logger.info("Registered notification listener for user: {}", userId);
    }
    
    public void unregisterListener(String clientId) {
        listeners.remove(clientId);
        logger.info("Unregistered notification listener: {}", clientId);
    }
    
    public void sendNotificationToScholar(String scholarId, String notification) {
        NotificationListener listener = listeners.get("SCHOLAR_" + scholarId);
        if (listener != null) {
            executorService.submit(() -> {
                try {
                    listener.onNotification(notification);
                    logger.info("Sent notification to scholar {}: {}", scholarId, notification);
                } catch (Exception e) {
                    logger.error("Error sending notification to scholar {}", scholarId, e);
                }
            });
        } else {
            logger.warn("Scholar {} is not registered for notifications", scholarId);
        }
    }
    
    public void sendNotificationToUser(String userId, String notification) {
        NotificationListener listener = listeners.get("USER_" + userId);
        if (listener != null) {
            executorService.submit(() -> {
                try {
                    listener.onNotification(notification);
                    logger.info("Sent notification to user {}: {}", userId, notification);
                } catch (Exception e) {
                    logger.error("Error sending notification to user {}", userId, e);
                }
            });
        } else {
            logger.warn("User {} is not registered for notifications", userId);
        }
    }
    
    public void broadcastToAllScholars(String message) {
        for (String clientId : listeners.keySet()) {
            if (clientId.startsWith("SCHOLAR_")) {
                NotificationListener listener = listeners.get(clientId);
                if (listener != null) {
                    executorService.submit(() -> {
                        try {
                            listener.onNotification(message);
                        } catch (Exception e) {
                            logger.error("Error broadcasting to {}", clientId, e);
                        }
                    });
                }
            }
        }
        logger.info("Broadcasted message to all scholars: {}", message);
    }
    
    public interface NotificationListener {
        void onNotification(String message);
    }
    
    // Singleton instance
    private static FatwaNotificationService instance;
    
    public static synchronized FatwaNotificationService getInstance() {
        if (instance == null) {
            instance = new FatwaNotificationService();
        }
        return instance;
    }
} 