package com.example.notifyhub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity for notification audit log.
 * Demonstrates IntelliJ IDEA's database tooling with explicit code (no Lombok).
 */
@Entity
@Table(
    name = "notification_log",
    indexes = {
        @Index(name = "idx_notification_log_recipient", columnList = "recipient"),
        @Index(name = "idx_notification_log_status_sent_at", columnList = "status, sent_at"),
        @Index(name = "idx_notification_log_notification_id", columnList = "notification_id")
    }
)
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false, length = 100)
    private String notificationId;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false, length = 50)
    private String channel;

    @Column(name = "template_code", length = 100)
    private String templateCode;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected NotificationLog() {
        // JPA requires no-arg constructor
    }

    public NotificationLog(String notificationId, String recipient, String channel,
                          String templateCode, String status) {
        this.notificationId = notificationId;
        this.recipient = recipient;
        this.channel = channel;
        this.templateCode = templateCode;
        this.status = status;
        this.sentAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    // Explicit getters (no Lombok)
    public Long getId() {
        return id;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getChannel() {
        return channel;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationLog that = (NotificationLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NotificationLog{" +
                "id=" + id +
                ", notificationId='" + notificationId + '\'' +
                ", recipient='" + recipient + '\'' +
                ", channel='" + channel + '\'' +
                ", status='" + status + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}
