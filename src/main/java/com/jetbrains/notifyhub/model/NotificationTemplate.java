package com.jetbrains.notifyhub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity for notification templates.
 * Demonstrates IntelliJ IDEA's database tooling with explicit code (no Lombok).
 * Shows entity navigation, JPQL autocomplete, and schema awareness.
 */
@Entity
@Table(
    name = "notification_template",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_template_channel_code_locale",
        columnNames = {"channel", "template_code", "locale"}
    )
)
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String channel;

    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    @Column(name = "subject_template", length = 500)
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(nullable = false, length = 10)
    private String locale = "en";

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected NotificationTemplate() {
        // JPA requires no-arg constructor
    }

    public NotificationTemplate(String channel, String templateCode, String bodyTemplate, String locale) {
        this.channel = channel;
        this.templateCode = templateCode;
        this.bodyTemplate = bodyTemplate;
        this.locale = locale;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Explicit getters (no Lombok)
    public Long getId() {
        return id;
    }

    public String getChannel() {
        return channel;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public String getLocale() {
        return locale;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTemplate that = (NotificationTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NotificationTemplate{" +
                "id=" + id +
                ", channel='" + channel + '\'' +
                ", templateCode='" + templateCode + '\'' +
                ", locale='" + locale + '\'' +
                ", active=" + active +
                '}';
    }
}
