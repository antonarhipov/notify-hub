package com.example.notifyhub.repository;

import com.example.notifyhub.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for NotificationLog entities.
 * Provides basic CRUD operations and audit trail queries.
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Find all logs for a specific recipient.
     */
    List<NotificationLog> findByRecipient(String recipient);

    /**
     * Find logs by notification ID.
     */
    List<NotificationLog> findByNotificationId(String notificationId);

    /**
     * Find logs by status with date range.
     */
    @Query("SELECT l FROM NotificationLog l WHERE l.status = :status AND l.sentAt BETWEEN :startDate AND :endDate ORDER BY l.sentAt DESC")
    List<NotificationLog> findByStatusAndDateRange(
        @Param("status") String status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
