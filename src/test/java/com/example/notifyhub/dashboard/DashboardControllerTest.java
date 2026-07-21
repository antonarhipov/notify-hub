package com.example.notifyhub.dashboard;

import com.example.notifyhub.IntegrationTestBase;
import com.example.notifyhub.service.ChannelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for the mission control dashboard REST API.
 */
@AutoConfigureMockMvc
class DashboardControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChannelService channelService;

    @BeforeEach
    void setUp() {
        channelService.getChannels().forEach(channel -> channelService.setEnabled(channel, true));
    }

    @Test
    void getStats_returnsAggregatedPayload() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").exists())
                .andExpect(jsonPath("$.successRate").exists())
                .andExpect(jsonPath("$.byChannel").isArray());
    }

    @Test
    void getServices_listsChannelsWithState() throws Exception {
        mockMvc.perform(get("/api/dashboard/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.channel == 'email')].enabled").value(true));
    }

    @Test
    void disableThenEnable_updatesServiceState() throws Exception {
        mockMvc.perform(post("/api/dashboard/services/sms/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channel").value("sms"))
                .andExpect(jsonPath("$.enabled").value(false));

        assertThat(channelService.isEnabled("sms")).isFalse();

        mockMvc.perform(post("/api/dashboard/services/sms/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        assertThat(channelService.isEnabled("sms")).isTrue();
    }

    @Test
    void toggleUnknownChannel_returnsNotFound() throws Exception {
        mockMvc.perform(post("/api/dashboard/services/carrier-pigeon/disable"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addChannel_registersNewEnabledChannel() throws Exception {
        mockMvc.perform(post("/api/dashboard/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channel\":\"webhook\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.channel").value("webhook"))
                .andExpect(jsonPath("$.enabled").value(true));

        assertThat(channelService.isKnownChannel("webhook")).isTrue();
        assertThat(channelService.isEnabled("webhook")).isTrue();
    }

    @Test
    void addExistingChannel_returnsConflict() throws Exception {
        mockMvc.perform(post("/api/dashboard/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channel\":\"email\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void addBlankChannel_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/dashboard/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channel\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }
}
