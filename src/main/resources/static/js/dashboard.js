(function () {
    "use strict";

    const API = "/api/dashboard";
    const REFRESH_MS = 10000;

    const CHANNEL_ICONS = {
        email: "✉",
        sms: "💬",
        push: "🔔"
    };

    const el = (id) => document.getElementById(id);

    function setText(id, value) {
        const node = el(id);
        if (node) {
            node.textContent = value;
        }
    }

    function showToast(message, isError) {
        const toast = el("toast");
        toast.textContent = message;
        toast.classList.toggle("error", Boolean(isError));
        toast.classList.add("show");
        clearTimeout(showToast._t);
        showToast._t = setTimeout(() => toast.classList.remove("show"), 2600);
    }

    function setLiveStatus(ok) {
        const dot = el("status-dot");
        dot.classList.toggle("error", !ok);
        dot.title = ok ? "Live" : "Connection error";
    }

    async function fetchJson(url, options) {
        const res = await fetch(url, options);
        if (!res.ok) {
            const body = await res.text().catch(() => "");
            throw new Error(body || ("Request failed: " + res.status));
        }
        const text = await res.text();
        return text ? JSON.parse(text) : null;
    }

    function formatTime(isoLike) {
        if (!isoLike) {
            return "";
        }
        const d = new Date(isoLike);
        if (isNaN(d.getTime())) {
            return isoLike;
        }
        return d.toLocaleString();
    }

    function renderStats(stats) {
        setText("stat-total", stats.totalProcessed);
        setText("stat-sent", stats.sent);
        setText("stat-failed", stats.failed);
        setText("stat-blocked", stats.blocked);
        setText("stat-rate", stats.successRate + "%");

        renderChannelTable(stats.byChannel || []);
        renderActivity(stats.recent || []);
    }

    function renderChannelTable(rows) {
        const tbody = el("channel-tbody");
        tbody.innerHTML = "";
        if (rows.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="5">No data yet</td></tr>';
            return;
        }
        rows.forEach((row) => {
            const tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="channel-name">' + escapeHtml(row.channel) + "</td>" +
                "<td>" + row.total + "</td>" +
                "<td>" + row.sent + "</td>" +
                "<td>" + row.failed + "</td>" +
                "<td>" + row.blocked + "</td>";
            tbody.appendChild(tr);
        });
    }

    function renderActivity(items) {
        const feed = el("activity-feed");
        feed.innerHTML = "";
        if (items.length === 0) {
            feed.innerHTML = '<li class="empty">No notifications processed yet.</li>';
            return;
        }
        items.forEach((item) => {
            const li = document.createElement("li");
            const status = (item.status || "").toLowerCase();
            li.innerHTML =
                '<span class="pill ' + status + '">' + escapeHtml(item.status) + "</span>" +
                "<span>" + escapeHtml(item.recipient) + " · " +
                escapeHtml(item.channel) + " · " + escapeHtml(item.templateCode || "") + "</span>" +
                '<span class="activity-meta">' + escapeHtml(formatTime(item.sentAt)) + "</span>";
            feed.appendChild(li);
        });
    }

    function renderServices(services) {
        const list = el("service-list");
        list.innerHTML = "";
        if (!services || services.length === 0) {
            list.innerHTML = '<p class="empty">No services registered.</p>';
            return;
        }
        services.forEach((service) => {
            const icon = CHANNEL_ICONS[service.channel] || "◉";
            const row = document.createElement("div");
            row.className = "service-row";

            const info = document.createElement("div");
            info.className = "service-info";
            info.innerHTML =
                '<span class="service-icon">' + icon + "</span>" +
                "<div>" +
                '<div class="service-name">' + escapeHtml(service.channel) + "</div>" +
                '<div class="service-state ' + (service.enabled ? "on" : "off") + '">' +
                (service.enabled ? "Enabled" : "Disabled") + "</div>" +
                "</div>";

            const label = document.createElement("label");
            label.className = "switch";
            const input = document.createElement("input");
            input.type = "checkbox";
            input.checked = service.enabled;
            input.addEventListener("change", () => toggleService(service.channel, input));
            const slider = document.createElement("span");
            slider.className = "slider";
            label.appendChild(input);
            label.appendChild(slider);

            row.appendChild(info);
            row.appendChild(label);
            list.appendChild(row);
        });
    }

    async function toggleService(channel, input) {
        const enabled = input.checked;
        input.disabled = true;
        try {
            const action = enabled ? "enable" : "disable";
            await fetchJson(API + "/services/" + encodeURIComponent(channel) + "/" + action, {
                method: "POST"
            });
            showToast("Service '" + channel + "' " + (enabled ? "enabled" : "disabled"));
            await Promise.all([loadServices(), loadStats()]);
        } catch (err) {
            input.checked = !enabled;
            showToast("Failed to update '" + channel + "': " + err.message, true);
        } finally {
            input.disabled = false;
        }
    }

    async function loadStats() {
        const stats = await fetchJson(API + "/stats");
        renderStats(stats);
    }

    async function loadServices() {
        const services = await fetchJson(API + "/services");
        renderServices(services);
    }

    async function refreshAll() {
        try {
            await Promise.all([loadStats(), loadServices()]);
            setLiveStatus(true);
            setText("last-updated", "Updated " + new Date().toLocaleTimeString());
        } catch (err) {
            setLiveStatus(false);
            showToast("Failed to load dashboard: " + err.message, true);
        }
    }

    function escapeHtml(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    document.addEventListener("DOMContentLoaded", () => {
        el("refresh-btn").addEventListener("click", refreshAll);
        refreshAll();
        setInterval(refreshAll, REFRESH_MS);
    });
})();
