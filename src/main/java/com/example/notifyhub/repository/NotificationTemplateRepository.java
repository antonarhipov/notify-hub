package com.example.notifyhub.repository;

import com.example.notifyhub.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for NotificationTemplate entities.
 * Demonstrates IntelliJ IDEA's database tooling features:
 * - JPQL autocomplete for entity names and fields
 * - Native SQL autocomplete for table and column names
 * - Schema validation and navigation
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    //DEMO Database Tooling - JPQL Query with Entity Autocomplete
    //DEMO Type: "SELECT t FROM Notif" and see NotificationTemplate suggested
    //DEMO Type: "WHERE t.ch" and see 'channel' field suggested
    /**
     * JPQL query demonstrating entity and field autocomplete.
     * IntelliJ will provide autocomplete for:
     * - NotificationTemplate entity name
     * - Properties: channel, active, etc.
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.channel = :channel AND t.active = true")
    List<NotificationTemplate> findActiveByChannel(@Param("channel") String channel);

    //DEMO Database Tooling - Native SQL Query with Table/Column Autocomplete
    //DEMO Type: "FROM notif" and see 'notification_template' table suggested
    //DEMO Type: "WHERE temp" and see 'template_code' column suggested
    //DEMO Introduce typo like "template_cod" to see red squiggle validation error
    //DEMO Ctrl+Click on 'notification_template' to navigate to schema.sql
    /**
     * Native SQL query demonstrating table and column autocomplete.
     * IntelliJ will provide autocomplete for:
     * - notification_template table name
     * - Columns: template_code, locale, active, channel
     * - Schema validation (red squiggle on typos)
     */
    @Query(value = "SELECT * FROM notification_template WHERE template_code = :code AND locale = :locale AND channel = :channel AND active = true LIMIT 1",
           nativeQuery = true)
    Optional<NotificationTemplate> findByCodeLocaleAndChannel(@Param("code") String code, @Param("locale") String locale, @Param("channel") String channel);

    /**
     * Method query demonstrating Spring Data JPA method name parsing.
     * IntelliJ will validate the method name and suggest completions.
     */
    List<NotificationTemplate> findAllByTemplateCode(String templateCode);

    /**
     * Additional method query for finding active templates by code and locale.
     */
    Optional<NotificationTemplate> findByTemplateCodeAndLocaleAndActiveTrue(String templateCode, String locale);
}
