package com.example.notifyhub.api;

import com.example.notifyhub.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that the notification API is locked down to the {@code ADMIN} role.
 */
@AutoConfigureMockMvc
class NotificationControllerSecurityTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendNotification_withoutCredentials_isUnauthorized() throws Exception {
        mockMvc.perform(post("/api/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sendNotification_asNonAdmin_isForbidden() throws Exception {
        mockMvc.perform(post("/api/notify")
                        .with(user("bob").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sendNotification_asAdmin_passesAuthorization() throws Exception {
        // An empty body fails bean validation (400); reaching validation proves the
        // request cleared the security layer rather than being rejected as 401/403.
        mockMvc.perform(post("/api/notify")
                        .with(user("alice").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_withAdminBasicAuth_passesAuthorization() throws Exception {
        mockMvc.perform(post("/api/notify")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void health_isRestrictedToAdmin() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isUnauthorized());
    }
}
